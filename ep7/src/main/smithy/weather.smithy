$version: "2"

namespace hello

use alloy#simpleRestJson

@simpleRestJson
service WeatherService {
    operations: [GetWeather, CreateCity]
    errors: [InternalServerError]
}

@http(method: "GET", uri: "/cities/{cityId}/weather", code: 201)
@readonly
operation GetWeather {
    input := {
        @required
        @httpLabel
        cityId: CityId
        // @httpHeader("SWS-Region")
        @httpQuery("region")
        @required
        region: String
    }
    output := {
        @required
        @httpHeader("SWS-Region")
        weather: String
        @httpPayload
        city: City
    }
    errors: [CityNotFoundError]
}

@error("client")
@httpError(404)
structure CityNotFoundError {
    reason: String
}

@error("server")
@httpError(500)
structure InternalServerError {
    reason: String
}

@http(method: "POST", uri: "/cities", code: 201)
operation CreateCity {
    input := {
        @required
        city: String
        @required
        country: String
    }
    output := {
        @required
        cityId: CityId
    }
}

structure City {
    @required
    name: String
    @required
    id: CityId
}

string CityId
