import jip.server.User
import jip.server.Job
import jip.server.State
import jip.server.Message
import jip.server.FinishState
import jip.server.Environment

includeTargets << grailsScript("_GrailsInit")

target(main: "Load Demo Jobs") {
    User user = User.findByUsername("user")
    if(user != null){
        load_demo_jobs(user)
    }
}

setDefaultTarget(main)

/**
 * Development helper to load some demo jobs
 */
def load_demo_jobs(User user) {
    for (int i = 0; i < 1000; i += 10) {
        println("Bootstrapping demo jobs ${i}")
        Job j = new Job()
        j.ownerId = user.id
        j.clusterId = 1 + i
        j.name = "Job Name"
        j.command = ["./run", "--local", "--mode"]
        j.cluster = "local"
        j.archived = false
        j.createDate = new Date(new Date().time - 35814851)
        j.startDate = new Date(new Date().time - 25814851)
        j.state = State.Running
        j.messages = [
                new Message(createDate: new Date(new Date().time - 24814851), message: "The job was started successfully"),
                new Message(createDate: new Date(new Date().time - 23814851), message: "I am processing a lot of data right now, let me give you an"
                        + " overview of the things I am doing right now. First of all, I am checking the "
                        + "files, then we will uncompress them and finally, I think I am going to"
                        + "read them !"),
                new Message(createDate: new Date(new Date().time - 22814851), type: Message.Type.WARNING, message: "One of the files is not yours !"),
                new Message(createDate: new Date(new Date().time - 21814851), type: Message.Type.ERROR, message: "Unable to read on of the files")
        ]
        j.save(validate: false, flush: true)

        j = new Job()
        j.ownerId = user.id
        j.clusterId = 2 + i
        j.name = "Job Name 2"
        j.command = ["./run2", "--local", "--mode"]
        j.cluster = "local"
        j.archived = false
        j.createDate = new Date(new Date().time - 15814851)
        j.startDate = new Date(new Date().time - 15814851)
        j.state = State.Running
        j.progress = 40
        j.messages = [
                new Message(createDate: new Date(new Date().time - 14814851), message: "The job was started successfully"),
                new Message(createDate: new Date(new Date().time - 13814851), message: "I am processing a lot of data right now, let me give you an"
                        + " overview of the things I am doing right now. First of all, I am checking the "
                        + "files, then we will uncompress them and finally, I think I am going to"
                        + "read them !"),
                new Message(createDate: new Date(new Date().time - 12814851), type: Message.Type.WARNING, message: "One of the files is not yours !"),
                new Message(createDate: new Date(new Date().time - 11814851), type: Message.Type.ERROR, message: "Unable to read on of the files")
        ]
        j.stderrPreview = """This is the gloriaous error log file
of a simple job that does
not contain too mucch information !"""
        j.stdoutPreview = """This is the gloriaous stdout log file
of a simple job that does
not contain too mucch information !
And this is a veeery long lins with a repeating pattern And this is a veeery long lins with a repeating pattern And this is a veeery long lins with a repeating pattern And this is a veeery long lins with a repeating pattern
And this is a veeery long lins with a repeating pattern
And this is a veeery long lins with a repeating pattern
And this is a veeery long lins with a repeating pattern
And this is a veeery long lins with a repeating pattern
And this is a veeery long lins with a repeating pattern
And this is a veeery long lins with a repeating pattern
And this is a veeery long lins with a repeating pattern
And this is a veeery long lins with a repeating pattern
And this is a veeery long lins with a repeating pattern
And this is a veeery long lins with a repeating pattern
And this is a veeery long lins with a repeating pattern
And this is a veeery long lins with a repeating pattern
And this is a veeery long lins with a repeating pattern
And this is a veeery long lins with a repeating pattern
And this is a veeery long lins with a repeating pattern
And this is a veeery long lins with a repeating pattern
And this is a veeery long lins with a repeating pattern
And this is a veeery long lins with a repeating pattern
And this is a veeery long lins with a repeating pattern
And this is a veeery long lins with a repeating pattern
"""
        j.save(validate: false, flush: true)

        j = new Job()
        j.ownerId = user.id
        j.name = "Submitted Job"
        j.command = ["./run2", "--local", "--mode"]
        j.cluster = "local"
        j.archived = false
        j.createDate = new Date(new Date().time - 12814851)
        j.state = State.Submitted
        j.save(validate: false, flush: true)


        j = new Job()
        j.ownerId = user.id
        j.clusterId = 4 + i
        j.name = "Queued Job"
        j.command = ["./run2", "--local", "--mode"]
        j.cluster = "local"
        j.archived = false
        j.createDate = new Date(new Date().time - 15814851)
        j.submitDate = new Date(new Date().time - 15614851)
        j.state = State.Queued
        j.messages = [
                new Message(createDate: new Date(new Date().time - 14814851), message: "Job is qued :)"),
        ]
        j.save(validate: false, flush: true)


        j = new Job()
        j.ownerId = user.id
        j.clusterId = 5 + i
        j.name = "Hold job"
        j.command = ["./run2", "--local", "--mode"]
        j.cluster = "local"
        j.archived = false
        j.createDate = new Date(new Date().time - 15814851)
        j.state = State.Hold
        j.messages = [
                new Message(createDate: new Date(new Date().time - 14814851), message: "Job went on hold"),
        ]
        j.save(validate: false, flush: true)

        j = new Job()
        j.ownerId = user.id
        j.clusterId = 6 + i
        j.name = "Completed Succesfully"
        j.command = ["./run2", "--local", "--mode"]
        j.cluster = "local"
        j.archived = false
        j.createDate = new Date(new Date().time - 15814851)
        j.submitDate = new Date(new Date().time - 15614851)
        j.finishDate = new Date(new Date().time - 14614851)
        j.state = State.Done
        j.progress = 10
        j.finishState = FinishState.Success
        j.messages = [
                new Message(createDate: new Date(new Date().time - 14814851), message: "Job completed succesfully"),
        ]
        j.save(validate: false, flush: true)

        j = new Job()
        j.ownerId = user.id
        j.clusterId = 7 + i
        j.name = "Completed Succesfully with progress"
        j.command = ["./run2", "--local", "--mode"]
        j.cluster = "local"
        j.archived = false
        j.createDate = new Date(new Date().time - 15814851)
        j.submitDate = new Date(new Date().time - 15614851)
        j.finishDate = new Date(new Date().time - 14614851)
        j.state = State.Done
        j.progress = 90
        j.finishState = FinishState.Success
        j.save(validate: false, flush: true)

        j = new Job()
        j.ownerId = user.id
        j.clusterId = 8 + i
        j.name = "Running with progress"
        j.command = ["./run2", "--local", "--mode"]
        j.cluster = "local"
        j.archived = false
        j.createDate = new Date(new Date().time - 15814851)
        j.submitDate = new Date(new Date().time - 15614851)
        j.startDate = new Date(new Date().time - 25814851)
        j.jobEnvironment = new Environment()
        j.jobEnvironment.cpus = 5
        j.jobEnvironment.nodes = 2
        j.jobEnvironment.partition = "Personal"
        j.jobEnvironment.time = 3600
        j.state = State.Running
        j.messages = [
                new Message(createDate: new Date(new Date().time - 14814851), progress: 10),
                new Message(createDate: new Date(new Date().time - 14804851), progress: 50),
        ]
        j.save(validate: false, flush: true)

        j = new Job()
        j.ownerId = user.id
        j.clusterId = 9 + i
        j.name = "Failed Job"
        j.command = ["./run2", "--local", "--mode"]
        j.cluster = "local"
        j.archived = false
        j.createDate = new Date(new Date().time - 15814851)
        j.submitDate = new Date(new Date().time - 15614851)
        j.finishDate = new Date(new Date().time - 14614851)
        j.state = State.Done
        j.finishState = FinishState.Error
        j.messages = [
                new Message(createDate: new Date(new Date().time - 14814851), message: "Job completed with problems"),
        ]
        j.save(validate: false, flush: true)


        j = new Job()
        j.ownerId = user.id
        j.clusterId = 10 + i
        j.name = "Canceled Job"
        j.command = ["./run2", "--local", "--mode"]
        j.cluster = "local"
        j.archived = false
        j.createDate = new Date(new Date().time - 15814851)
        j.submitDate = new Date(new Date().time - 15614851)
        j.finishDate = new Date(new Date().time - 14614851)
        j.state = State.Done
        j.finishState = FinishState.Cancel
        j.messages = [
                new Message(createDate: new Date(new Date().time - 14814851), message: "Job Canceled"),
        ]
        j.save(validate: false, flush: true)


        Job p1 = createDemoJob("Pipeline Job 1", 11 + i, State.Done, user)
        p1.finishState = FinishState.Success
        p1.command = ["ls", "-l"]
        p1.inPipeline = true
        p1.messages = [
                new Message(createDate: new Date(), message: "Job Done"),
        ]
        p1 = p1.save(validate: false, flush: true)

        Job p2 = createDemoJob("Pipeline Job 2", 12 + i, State.Running, user)
        p2.inPipeline = true
        p2.command = ["wc", "-l"]
        p2.dependsOn = [p1.id]
        p2.messages = [
                new Message(createDate: new Date(), message: "Start counting"),
        ]
        p2 = p2.save(validate: false, flush: true)

        Job pipeline = createDemoJob("Pipeline Job", 13 + i, State.Running, user)
        pipeline.pipelineJobs = [p1.id, p2.id]
        pipeline.save(validate: false, flush: true)

    }
}


Job createDemoJob(String name, Long clusterId, State state, User user) {
    Job j = new Job()
    j.ownerId = user.id
    if (clusterId) j.clusterId = clusterId
    j.name = name
    j.cluster = "local"
    j.archived = false
    j.createDate = new Date(new Date().time - 10000)
    j.state = state
    switch (state) {
        case State.Queued:
            j.submitDate = new Date(j.createDate.time + 2000)
            break
        case State.Running:
            j.submitDate = new Date(j.createDate.time + 2000)
            j.startDate = new Date(j.createDate.time + 3000)
            break
        case State.Done:
            j.submitDate = new Date(j.createDate.time + 2000)
            j.startDate = new Date(j.createDate.time + 3000)
            j.finishDate = new Date(j.createDate.time + 5000l)
            break
    }
    return j
}
