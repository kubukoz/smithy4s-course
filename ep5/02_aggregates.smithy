$version: "2"

namespace aggregates

use simpleshapes#MyString
use simpleshapes#MyTimestamp
// 
// Aggregate shapes. These are shapes that contain other shapes (members).
// https://smithy.io/2.0/spec/aggregate-types.html
// 
// 
/// A structure is a collection of 0..N named members.
/// By default, all members are considered optional, and they can be made required with the `@required` trait.

structure MyStructure {
    @required
    greeting: String
    /// Your custom shapes can be used as members!
    // Note: they have to be imported if they come from a different namespace.
    addressee: MyString
    createdAt: MyTimestamp
    /// Structure members can have default values: if the input doesn't include a given member, that default can be used.
    identifier: String = ""
}

/// A union is a collection of 0..N named members, of which only one can be present in the input.
/// Essentially an evolution of enums, but with more flexibility (the possible values can be any shape, not just strings).
union MyUnion {
    /// By default, most protocols encode unions as tagged unions, where the tag is the member name.
    /// For example, a possible encoding of the "i" case with the integer 42 could be:
    /// { "i": 42 }
    i: Integer
    s: String
    b: Boolean
    ms: MyStructure
}

/// Sequences are also supported, with list shapes.
/// A list shape must declare exactly one member, with the name "member". That's the shape of the list's elements.
/// By default, all elements are non-nullable. This can be controlled with the `@sparse` trait (if the protocol supports it).
list MyStructures {
    member: MyStructure
}

/// Other kinds of sequences are supported, too.
/// In order to get a "Set" shape, you can use the `@uniqueItems` trait.
/// There's also `@vector` and `@indexedSeq` for other kinds of sequences.
@uniqueItems
list MyUniqueStructures {
    member: MyStructure
}

/// Aggregate shapes can be (also mutually) recursive.
/// The only catch is that Smithy must be able to tell that a value of the shape can actually be constructed,
/// i.e. it doesn't only contain self-recursion.
union MyRecUnion {
    rec: MyRecStruct
    end: Unit
}

structure MyRecStruct {
    @required
    u: MyRecUnion
    /// This wouldn't be allowed as a required field because no value of the struct could be constructed
    self: MyRecStruct
}
