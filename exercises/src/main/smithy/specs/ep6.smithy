$version: "2"

namespace specs.ep6

use alloy#simpleRestJson

@simpleRestJson
service StudentService {
    operations: [
        GetStudent
        CreateStudent
        ListStudents
        ListClasses
    ]
}

@http(method: "GET", uri: "/students/{id}")
@readonly
operation GetStudent {
    input := {
        @httpLabel
        @required
        id: String
    }
    output := {
        @required
        name: String
    }
}

@http(method: "POST", uri: "/students")
operation CreateStudent {
    input := {
        @required
        name: String
    }
    output := {
        @required
        id: String
        @required
        name: String
    }
}

@http(method: "GET", uri: "/students")
@readonly
operation ListStudents {
    input := {
        @httpQuery("limit")
        maxStudents: Integer
    }
    output := {
        @required
        students: Students
    }
}

list Students {
    member: Student
}

structure Student {
    @required
    name: String
}

@http(method: "GET", uri: "/classes")
@readonly
operation ListClasses {
    output := {
        @httpPayload
        @required
        classes: Classes
    }
}

list Classes {
    member: Class
}

structure Class {
    @required
    name: String
    @required
    teacher: String
}
