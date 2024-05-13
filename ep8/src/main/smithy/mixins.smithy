$version: "2"

namespace mixins

use smithy4s.meta#adt

structure User {
    @required
    displayName: String
    avatarUrl: String
    @required
    data: UserData
}

@mixin
structure UserLike {
    @required
    displayName: String
    avatarUrl: String
}

@mixin
structure AdminUserLike with [UserLike] {}

@adt
union UserData {
    unauthorized: UnauthorizedUserData
    authorized: AuthorizedUserData
    admin: AdminUserData
}

structure UnauthorizedUserData with [UserLike] {
    @required
    ipAddress: String
}

structure AuthorizedUserData with [UserLike] {
    @required
    userId: String
}

structure AdminUserData with [AdminUserLike] {
    @required
    userId: String
    @required
    roles: Roles
    @required
    $avatarUrl
}

list Roles {
    member: String
}

/// foo
@default("foobar")
@mixin
string NameLike

/// asdg
string Name with [NameLike]

string FullName with [NameLike]
