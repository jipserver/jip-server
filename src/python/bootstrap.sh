#!/bin/bash
# This is the JIP client bootstrap script
# which is used to install the JIP client python command
# line tools and libraries.

## check for python
command -v python >/dev/null 2>&1 || { echo >&2 "I require python but it's not installed.  Aborting."; exit 1; }

## deactivate any currently active environment
deactivate

echo "Current working directory: "`pwd`
echo "Environment"
set
## create virtual environment
echo "Creating JIP virtual environment"
python src/virtualenv/virtualenv.py --clear . 2>&1 || { echo >&2 "Creating virtual environment failed! Aborting"; exit 1; }

echo "Adding JIP variables"
echo "export JIP_CLUSTER=\"$1\"" >> bin/activate
echo "export JIP_SERVER=\"$2\"" >> bin/activate

echo "Activating the environment"
. bin/activate

## prepare execs
chmod +x bin/*

## pip install python dependencies
echo "Installing dependencies"
pip install requests

## create folder structure
## and update executables
chmod a+x bin/*

echo "Installing the jip client"
cd src/jip-client/
python setup.py install 2>&1 || { echo >&2 "Installing jip client failed! Aborting"; exit 1; }
cd ../../

cwd=`pwd`
echo ""
echo "#########"
echo "## Jip environment created"
echo "## In order to activat it manually, from teh client type"
echo "## . ${cwd}/bin/activate"
