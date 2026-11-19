import { Alert, Anchor, Badge, Button, Card, Container, Group, Stack, Text, Title } from '@mantine/core'
import { useEffect, useState } from 'react'
import { Link } from 'react-router'
import { eventTypesApi } from '../../shared/api/client'
import type { EventType } from '../../shared/api/generated'
import { formatDuration } from '../../shared/lib/datetime'
import { getErrorMessage } from '../../shared/lib/errors'

export function HomePage() {
  // null while loading, so "no event types" isn't shown before the list arrives.
  const [eventTypes, setEventTypes] = useState<EventType[] | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    eventTypesApi
      .eventTypesList()
      .then(setEventTypes)
      .catch(async (e: unknown) => setError(await getErrorMessage(e)))
  }, [])

  return (
    <Container size="md" py="md">
      <Group justify="flex-end">
        <Anchor component={Link} to="/admin" size="sm" c="dimmed">
          Кабинет владельца
        </Anchor>
      </Group>
      <Stack>
        <Title order={1}>Календарь звонков</Title>
        <Text>Выберите встречу и запишитесь на удобное время без регистрации.</Text>
        {error && (
          <Alert color="red" title="Не удалось загрузить встречи">
            {error}
          </Alert>
        )}
        {eventTypes?.length === 0 && <Text c="dimmed">Владелец пока не открыл запись.</Text>}
        {eventTypes?.map((eventType) => (
          <Card key={eventType.id} withBorder>
            <Title order={3}>{eventType.title}</Title>
            <Badge variant="light">{formatDuration(eventType.durationMinutes)}</Badge>
            {eventType.description && (
              <Text size="sm" c="dimmed" lineClamp={3}>
                {eventType.description}
              </Text>
            )}
            <Button component={Link} to={`/book/${eventType.slug}`}>
              Записаться
            </Button>
          </Card>
        ))}
      </Stack>
    </Container>
  )
}
