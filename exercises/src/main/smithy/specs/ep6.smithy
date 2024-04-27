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
        ListTeachers
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
        @required
        @httpHeader("MY-USER-ID")
        userId: String
    }
    output := {
        @required
        id: String
        @required
        name: String
    }
}

@http(method: "POST", uri: "/students/search")
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

@http(method: "GET", uri: "/teachers")
@readonly
operation ListTeachers {
    output := {
        @required
        @httpPayload
        teachers: Teachers
    }
}

list Teachers {
    member: Teacher
}

structure Teacher {
    @jsonName("teacherName")
    @required
    name: String
}
