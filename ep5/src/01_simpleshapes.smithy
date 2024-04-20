$version: "2"

metadata suppressions = [{
    id: "UnreferencedShape"
    namespace: "simpleshapes"
    reason: "This is a tutorial"
}]
namespace simpleshapes

use alloy#openEnum
// 
// Simple shapes. These are "flat" shapes, i.e. they don't include any others.
// https://smithy.io/2.0/spec/simple-types.html
// 
// 
/// For serialization purposes, equivalent to any other string.
/// Useful for type safety or attaching traits to all references of this shape.

@length(min: 1)
string MyString

boolean MyBoolean

/// You can also define integers and other numeric types.
integer MyInt

byte MyByte

short MyShort

long MyLong

float MyFloat

double MyDouble

bigInteger MyBigInteger

bigDecimal MyBigDecimal

/// Byte arrays are also supported.
/// These are usually represented as raw bytes, or (if need be), "escaped" as a base64-encoded string (e.g. in JSON).
blob MyByteArray

/// Documents are free-form shapes modelling essentially "schemaless" values.
/// Structurally equivalent to a JSON value (object, number, array, string, boolean, null).
/// In protocols that use JSON, documents are indeed represented as JSON values.
document MyDocument

/// Enums are strings limited to a set to 1..N values.
// 
// Note: the Smithy spec considers enums as open, meaning that consumers should be able to handle unknown values.
// Smithy4s considers enum closed by default.
enum MyEnum {
    HELLO
    GOODBYE
    /// Custom values can be specified.
    GOOD_AFTERNOON = "good afternoon"
}

// Smithy4s supports open enums starting from 0.18, but it requires a trait.
@openEnum
enum MyOpenEnum {
    HELLO
    GOODBYE
    /// Custom values can be specified.
    GOOD_AFTERNOON = "good afternoon"
}

/// intEnums are like enums, but represented as integers.
/// Each value of an int enum must explicitly specify its integer value.
intEnum MyIntEnum {
    HELLO = 1
    GOODBYE = 2
}

/// An instant in time, independent of any locale or timezone.
/// Can be represented on the wire in multiple ways, e.g. as a string (ISO-8601), or as a number (seconds since epoch).
/// In many protocols, this behavior can be controlled with the timestampFormat trait.
timestamp MyTimestamp
