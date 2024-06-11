$version: "2"

namespace ep7

use alloy#simpleRestJson

@simpleRestJson
service HotelService {
    operations: [
        CreateBooking
        RetrieveBooking
        CancelBooking
    ]
    errors: [InternalServerError]
}

@http(method: "POST", uri: "/bookings")
operation CreateBooking {
    input := {
        @required
        @httpPayload
        data: NewBooking
    }
    output := {
        @required
        @httpPayload
        booking: Booking
    }
}

@http(method: "GET", uri: "/bookings/{id}")
@readonly
operation RetrieveBooking {
    input := {
        @httpLabel
        @required
        id: String
    }
    output := {
        @required
        @httpPayload
        booking: Booking
    }
    errors: [BookingNotFound]
}

@http(method: "DELETE", uri: "/bookings/{id}")
@idempotent
operation CancelBooking {
    input := {
        @httpLabel
        @required
        id: String
    }
    errors: [BookingNotFound]
}

// We haven't talked about mixins yet, so you can catch up:
// https://smithy.io/2.0/spec/mixins.html
// tl;dr all these members will be copied to whatever uses this mixin in its "with" clause.
@mixin
structure BookingDetails {
    @required
    customerId: String
    @required
    start: Timestamp
    @required
    end: Timestamp
    details: String
}

structure NewBooking with [BookingDetails] {}

structure Booking with [BookingDetails] {
    @required
    id: String
}

@error("server")
@httpError(500)
structure InternalServerError {
    description: String
}

@error("client")
@httpError(404)
structure BookingNotFound {}
