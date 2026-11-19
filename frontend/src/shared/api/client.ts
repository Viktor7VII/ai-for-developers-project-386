import {
  BookingsApi,
  DefaultApi,
  EventTypesApi,
  PublicBookingApi,
  ScheduleApi,
} from './generated'

export const api = new DefaultApi()

export const eventTypesApi = new EventTypesApi()

export const scheduleApi = new ScheduleApi()

export const bookingsApi = new BookingsApi()

export const publicBookingApi = new PublicBookingApi()
