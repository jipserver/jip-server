#!/usr/bin/env python
"""Python support library for a jip grid.
The module is used by the jip server to execute remote commands and jobs.
"""
from optparse import OptionParser
from optparse import Option
import optparse
import json
import os
import sys
import subprocess
import tempfile
import requests
import traceback
import logging

class ExecError(Exception):
    pass


class MultipleOption(Option, object):
    """
    Helper class to make optparser take
    multiple option values
    """
    ACTIONS = Option.ACTIONS + ("extend",)
    STORE_ACTIONS = Option.STORE_ACTIONS + ("extend",)
    TYPED_ACTIONS = Option.TYPED_ACTIONS + ("extend",)
    ALWAYS_TYPED_ACTIONS = Option.ALWAYS_TYPED_ACTIONS + ("extend",)

    def take_action(self, action, dest, opt, value, values, parser):
        if action == "extend":
            values.ensure_value(dest, []).append(value)
            for a in parser.rargs[:]:
                if a.startswith("-"):
                    break
                else:
                    values.ensure_value(dest, []).append(a)
                    parser.rargs.remove(a)

        else:
            Option.take_action(self, action, dest, opt, value, values, parser)


class NoErrOptionParser(optparse.OptionParser, object):
    """
    An unknown option pass-through implementation of OptionParser.

    When unknown arguments are encountered, bundle with largs and try again,
    until rargs is depleted.

    sys.exit(status) will still be called if a known argument is passed
    incorrectly (e.g. missing arguments or bad argument types, etc.)
    """

    def __init__(self):
        super(NoErrOptionParser, self).__init__(option_class=MultipleOption)

    def _process_args(self, largs, rargs, values):
        while rargs:
            try:
                OptionParser._process_args(self, largs, rargs, values)
            except (optparse.BadOptionError, optparse.AmbiguousOptionError), e:
                largs.append(e.opt_str)


class JipJob(object):
    def __init__(self, jobdef):
        """Create a new job from the given definition"""
        self.id = jobdef.get('id', None)
        self.command = jobdef.get('command', None)
        self.script = jobdef.get("script", None)
        self.url = jobdef.get("url", None)
        self.cluster = jobdef.get("cluster", None)
        self.token = jobdef.get("token", None)
        self.stdin = jobdef.get("stdin", None)
        self.cwd = jobdef.get("cwd", ".")
        logging.debug("Initialized job from %s" % (str(jobdef)))

        # export environment
        os.putenv('JIP_URL', str(self.url))
        os.putenv('JIP_JOB', str(self.id))
        os.putenv('JIP_TOKEN', str(self.token))
        os.putenv('JIP_CLUSTER', str(self.cluster))
        self.connection_info = _get_connection_info(
            url=self.url,
            cluster=self.cluster,
            user=self.id,
            password=self.token,
            job=self.id,
            check_env=False
        )

    def run(self):
        send_state("Running", connection_info=self.connection_info)
        exitCode = 1
        try:
            pin = None
            if self.stdin:
                pin = open(self.stdin, 'rb')

            input = self.command
            if self.script is not None:
                input = write_scipt(self.script)
                logging.debug("Script file written")

            process = subprocess.Popen(input, shell=True, stdin=pin, cwd=self.cwd)
            exitCode = process.wait()

            if self.script is not None and os.path.exists(input):
                os.remove(input)

            if exitCode != 0:
                send_state("Done", "Error", connection_info=self.connection_info)
            else:
                send_state("Done", "Success", connection_info=self.connection_info)

        except Exception, e:
            logging.error("Error while execution process %s" % (str(e)))
            send_message("ERROR", "%s"%str(e), connection_info=self.connection_info)
            send_state("Done", "Error", connection_info=self.connection_info)

        exit(exitCode)


def jip_exec(args):
    job = JipJob(json.load(sys.stdin))
    job.run()


def write_scipt(script):
    """Write script to temp file"""
    (fd, tmpfile) = tempfile.mkstemp(suffix=".sh", prefix="jip-script")
    logging.debug("Writing script to %s" % (tmpfile))
    fifo = open(tmpfile, 'w')
    fifo.write(script)
    fifo.close()
    os.close(fd)
    os.chmod(os.path.abspath(tmpfile), 0755)
    return tmpfile


def __prepare_submit_url(jip_url, api):
    if jip_url[-1] == "/": jip_url = jip_url[:-1]
    if api[0] == "/": api = api[1:]
    return "%s/api/%s" % (jip_url, api)


def __create_connection_options():
    parser = NoErrOptionParser()
    parser.add_option("--url", dest="url", help="Jip server url", default=None)
    parser.add_option("--cluster", dest="cluster", help="Jip cluster name", default=None)
    parser.add_option("--user", dest="user", help="Jip user name", default=None)
    parser.add_option("--password", dest="password", help="Jip password", default=None)
    return parser


def handle_reponse(response, quiet=False):
    logging.debug("JIP Server response: %s\n\t%s" % (response, response.text))
    if response.status_code == 200:
        return True
    elif response.status_code == 400:
        if not quiet: print >> sys.stderr, "ERROR : %s" % response.text
    elif response.status_code == 404:
        if not quiet: print >> sys.stderr, "Unknown API url, please check the jip client version/code, this might be a bug :("
    elif response.status_code == 401:
        if not quiet: print >> sys.stderr, "Access denied by server, check your username and password!"
    else:
        if not quiet: print >> sys.stderr, "Unknown server error %s" % response

    return False


def send(function, connection_info, data):
    """Send request to server using the given requests function"""
    logging.debug("Request to JIP url %s with data : %s" % (connection_info['url'], str(data)))
    logging.debug(connection_info)
    return function(
        connection_info['url'],
        data=data,
        headers={'content-type': 'application/json'},
        auth=(connection_info['user'], connection_info['password']),
        timeout=2
    )


def send_state(state, finishedState=None, connection_info=None):
    """Send job started to the jip server"""
    if connection_info is None:
        connection_info = _get_connection_info()
    connection_info = connection_info.copy()
    connection_info['url'] = __prepare_submit_url(connection_info['url'], "job/%s/state" % connection_info['job'])
    data = {
        'state': state
    }
    if finishedState is not None:
        data["finished"] = finishedState

    try:
        response = send(requests.put, connection_info, json.dumps(data))
        handle_reponse(response)
        if response.status_code != 200:
            logging.error("Unable to send state: %s" % (str(response)))
            return False
    except requests.exceptions.Timeout:
        logging.error("Connection timeout")
        return False
    return True


def jip_submit(args):
    """Submit a job from the command line
    Connection information can be put in args but
    is also read fom config file and environment
    """
    parser = __create_connection_options()
    parser.add_option("--cwd", dest="cwd", help="Working directory", default=os.path.abspath(os.curdir))

    programm_args = None
    if "--" in args:
        i = args.index("--")
        programm_args = args[i + 1:]
        args = args[:i]

    options, rest_args = parser.parse_args(args)
    connection_info = _get_connection_info(options)
    connection_info['url'] = __prepare_submit_url(connection_info['url'], "cluster/%s/submit" % connection_info['cluster'])

    job = {
        "command": " ".join(programm_args),
        "cwd": options.cwd
    }

    try:
        response = send(requests.post, connection_info, json.dumps(job))
        if not handle_reponse(response):
            exit(1)
        if response.status_code == 200:
            print "Job", response.json['id'], "submitted"
    except requests.exceptions.Timeout, e:
        print >> sys.stderr, "Connection timeout"
        exit(1)


def jip_send(args):
    """Send messages to the JIP server"""
    parser = __create_connection_options()
    options, rest_args = parser.parse_args(args)
    if len(rest_args) == 2:
        connection_info = _get_connection_info(options)
        if not send_message(args[0], args[1], connection_info):
            exit(1)
    else:
        logging.error("Unable to get message information from arguments %s" % args)
        exit(1)


def send_message(type, message, connection_info=None):
    """Sends messages to the server"""
    if connection_info is None:
        connection_info = _get_connection_info()
    connection_info = connection_info.copy()

    message = {
        "type": type.upper(),
        "message" : message
    }
    connection_info['url'] = __prepare_submit_url(connection_info['url'], "job/%s/message" % connection_info['job'])
    try:
        response = send(requests.put, connection_info, json.dumps(message))
        if not handle_reponse(response):
            return False
    except requests.exceptions.Timeout, e:
        logging.error("Connection timeout")
        return False
    return True



def _get_connection_info(options=None, url=None, cluster=None, user=None, password=None, job=None, check_env=True):
    """Load connection information. This
    first checks for an existing config file,
    then environment and then, if passed, the options
    """
    # initialize
    info = {
        'url': None,
        'cluster': None,
        'user':None,
        'password':None,
        'job':None
    }

    if check_env:
        ## read config
        cfg_file = "%s/.jipclient" % os.getenv("HOME")
        if os.path.exists(cfg_file):
            with open(cfg_file, 'r') as f:
                for line in f:
                    elements = line.strip().split("=")
                    if len(elements) != 2: continue
                    (key, value) = [ x.strip() for x in elements[:2] ]
                    if key in ['url', 'cluster', "user", "password"]:
                        info[key] = value

        ## update from environment
        from_env = {
            'url': os.getenv('JIP_URL', info['url']),
            'cluster': os.getenv('JIP_CLUSTER', info['cluster']),
            'user': os.getenv('JIP_USER', info['user']),
            'password': os.getenv('JIP_PASSWORD', info['password']),
        }
        info.update(from_env)

        ## update for user/password in case of JOB/TOKEN
        if os.getenv("JIP_JOB", None) is not None:
            info['job'] = os.getenv("JIP_JOB", None)
            info['user'] = os.getenv("JIP_JOB", None)
            info['password'] = os.getenv("JIP_TOKEN", None)
        elif options is not None:
            # update from options
            if options.user is not None: info['user'] = options.user
            if options.password is not None: info['password'] = options.password
            if options.cluster is not None: info['cluster'] = options.cluster
            if options.url is not None: info['url'] = options.url

    # update from parameter
    if user is not None: info['user'] = user
    if password is not None: info['password'] = password
    if url is not None: info['url'] = url
    if cluster is not None: info['cluster'] = cluster
    if job is not None: info['job'] = job

    logging.debug("Connection info: %s" % (str(info)))
    return info


def jip_help(args):
    """Print help"""
    print """JIP Command line client
    Available commands:
    """
    for k, v in __commands.items():
        print "    %s" % k
    exit(0)

# JIP commands
__commands = {
    "exec": jip_exec,
    "submit": jip_submit,
    "send": jip_send,
    "help": jip_help,
    }

def main():
    try:
        if os.getenv("JIP_LOGGING") is not None:
            loglevel = os.getenv("JIP_LOGGING")
            numeric_level = getattr(logging, loglevel.upper(), None)
            if not isinstance(numeric_level, int):
                raise ValueError('Invalid log level: %s' % loglevel)
            logging.basicConfig(level=numeric_level)

        cmd = None
        if len(sys.argv) >= 2:
            cmd = sys.argv[1]
        if cmd is None:
            sys.stderr.write("No command specified !\n")
            exit(1)
        elif not cmd in __commands.keys():
            sys.stderr.write("Unknown command %s !\n" % cmd)
            exit(1)
        else:
            ## extend python path to make sure the jip module
            ## is in path
            sys.path.append(os.path.split(os.path.abspath(__file__))[0])
            __commands[cmd](sys.argv[2:])
    except KeyboardInterrupt, interruped:
        exit(1)


if __name__ == "__main__":
    main()
