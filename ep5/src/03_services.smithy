$version: "2"

namespace services

// Service types. These compose other shapes into services that can be used to run some actions.
// Notably, unlike aggregate shapes they have no members - they have "properties" instead.
// https://smithy.io/2.0/spec/service-types.html
// 
/// An operation is a single action that can be performed by a service (usually one service, although you're free to reuse operations).
/// It can be thought of as a function signature, with input and output shapes.
operation MyOperation {
    /// input/output are optional properties, and they default to Unit (a special empty structure shape signifying no data).
    input: MyOperationInput
    output := {
        /// The := syntax (inline input/output) defines a structure shape for more concise definitions.
        /// Most of the time, an input/output is only used once, so there's no need to declare that shape explicitly.
        @required
        id: String
    }
}

structure MyOperationInput {
    @required
    greeting: String
}

/// Service shapes (the real ones :)) are logical grouping of operations.
/// If operation shapes are function signatures, services are interface definitions that declare these functions.
service MyService {
    version: "1.0"
    operations: [MyOperation, MyOtherOperation]
    // We'll get to these in a moment.
    resources: [MyResource]
}

operation MyOtherOperation {

}

/// Resource shapes define CRUD-like operations on an entity.
/// 
/// Resources are a somewhat complex topic that I have yet to fully grasp. They may get a separate episode in the future.
/// Not coincidentally, they're not very well supported in smithy4s (other than as a grouping of operations inside a service).
resource MyResource {
    // One of the properties of a resource is `read`, there are also predefined properties for other methods in CRUD.
    read: GetUser
    // The properties of the entity.
    properties: {
        name: String
    }
    // The identifiers that can be used to refer to the entity.
    identifiers: {
        userId: UserId
    }
}

string UserId

structure UserForRead for MyResource {
    $name
}

@readonly
operation GetUser {
    input := {
        @required
        userId: UserId
    }
    output: UserForRead
}
