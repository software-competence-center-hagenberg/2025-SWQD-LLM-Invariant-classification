### Swagger Definition Breakdown

- **Endpoint**: `/youtube/v3/videos`
- **Method**: `GET`
- **Parameters**:
  - `maxHeight`: An optional query parameter of type integer with a minimum value of 72 and a maximum length of 8192.
- **Response**:
  - Contains a list of resources, each with a `snippet` object.
  - The `snippet` object includes a `thumbnails` object.
  - The `thumbnails` object has a `medium` property, which is an object that includes a `width` property of type integer.

### Invariant

- **Invariant**: `input.maxHeight != return.snippet.thumbnails.medium.width`
- **Invariant Type**: `daikon.inv.binary.twoScalar.IntNonEqual`
- **Description**: This invariant suggests that the `maxHeight` query parameter is never equal to the `width` of the `medium` thumbnail in the response.

### Analysis

- **Number of Calls**: 10,000 calls were made to the API without finding a counterexample.
- **Distinct Examples**: 1,077 distinct examples were found where the invariant holds.
- **Example Values**:
  - `input.maxHeight=878; return.items.snippet.thumbnails.medium.width=320`
  - `input.maxHeight=2302; return.items.snippet.thumbnails.medium.width=320`
  - `input.maxHeight=3166; return.items.snippet.thumbnails.medium.width=320`
  - `input.maxHeight=6943; return.items.snippet.thumbnails.medium.width=320`
  - `input.maxHeight=2235; return.items.snippet.thumbnails.medium.width=320`

### Conclusion

The invariant `input.maxHeight != return.snippet.thumbnails.medium.width` suggests that the `maxHeight` parameter, which is intended to filter or limit the height of resources, does not directly correspond to the `width` of the `medium` thumbnail in the response. Given the semantic difference between a height parameter and a width property, it is reasonable to expect that these values would not be equal in most cases.

The fact that 10,000 calls were made without finding a counterexample, combined with the semantic difference between the two fields, strongly suggests that this invariant is a true-positive. The invariant holds due to the distinct purposes and typical values of `maxHeight` and `width` in the context of video thumbnails.

Therefore, the invariant is classified as **true-positive** with high confidence.
