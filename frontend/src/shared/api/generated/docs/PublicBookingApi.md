# PublicBookingApi

All URIs are relative to *http://localhost:8080*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**publicBookingCreateBooking**](PublicBookingApi.md#publicbookingcreatebooking) | **POST** /api/public/event-types/{slug}/bookings |  |
| [**publicBookingGetEventType**](PublicBookingApi.md#publicbookinggeteventtype) | **GET** /api/public/event-types/{slug} |  |
| [**publicBookingListSlots**](PublicBookingApi.md#publicbookinglistslots) | **GET** /api/public/event-types/{slug}/slots |  |



## publicBookingCreateBooking

> Booking publicBookingCreateBooking(slug, bookingRequest)



Book a slot. Availability is re-checked on the server; 409 is returned if the slot is taken, in the past or outside the schedule.

### Example

```ts
import {
  Configuration,
  PublicBookingApi,
} from '';
import type { PublicBookingCreateBookingRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new PublicBookingApi();

  const body = {
    // string
    slug: slug_example,
    // BookingRequest
    bookingRequest: ...,
  } satisfies PublicBookingCreateBookingRequest;

  try {
    const data = await api.publicBookingCreateBooking(body);
    console.log(data);
  } catch (error) {
    console.error(error);
  }
}

// Run the test
example().catch(console.error);
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **slug** | `string` |  | [Defaults to `undefined`] |
| **bookingRequest** | [BookingRequest](BookingRequest.md) |  | |

### Return type

[**Booking**](Booking.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: `application/json`
- **Accept**: `application/json`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **201** | The request has succeeded and a new resource has been created as a result. |  -  |
| **400** | Bad request: validation failed. |  -  |
| **404** | Resource not found. |  -  |
| **409** | Conflict with existing data. |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## publicBookingGetEventType

> EventType publicBookingGetEventType(slug)



Get public information about an event type.

### Example

```ts
import {
  Configuration,
  PublicBookingApi,
} from '';
import type { PublicBookingGetEventTypeRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new PublicBookingApi();

  const body = {
    // string
    slug: slug_example,
  } satisfies PublicBookingGetEventTypeRequest;

  try {
    const data = await api.publicBookingGetEventType(body);
    console.log(data);
  } catch (error) {
    console.error(error);
  }
}

// Run the test
example().catch(console.error);
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **slug** | `string` |  | [Defaults to `undefined`] |

### Return type

[**EventType**](EventType.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `application/json`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | The request has succeeded. |  -  |
| **404** | Resource not found. |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## publicBookingListSlots

> Array&lt;TimeSlot&gt; publicBookingListSlots(slug, from, to)



List free slots for an event type that start within [from, to). Slots in the past and slots overlapping existing bookings are excluded. The range must not exceed 62 days.

### Example

```ts
import {
  Configuration,
  PublicBookingApi,
} from '';
import type { PublicBookingListSlotsRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new PublicBookingApi();

  const body = {
    // string
    slug: slug_example,
    // Date
    from: 2013-10-20T19:20:30+01:00,
    // Date
    to: 2013-10-20T19:20:30+01:00,
  } satisfies PublicBookingListSlotsRequest;

  try {
    const data = await api.publicBookingListSlots(body);
    console.log(data);
  } catch (error) {
    console.error(error);
  }
}

// Run the test
example().catch(console.error);
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **slug** | `string` |  | [Defaults to `undefined`] |
| **from** | `Date` |  | [Defaults to `undefined`] |
| **to** | `Date` |  | [Defaults to `undefined`] |

### Return type

[**Array&lt;TimeSlot&gt;**](TimeSlot.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `application/json`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | The request has succeeded. |  -  |
| **400** | Bad request: validation failed. |  -  |
| **404** | Resource not found. |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)

