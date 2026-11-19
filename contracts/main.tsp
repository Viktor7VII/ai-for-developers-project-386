import "@typespec/http";
import "@typespec/rest";

using Http;
using Rest;

@service(#{ title: "Call Calendar API" })
@server("http://localhost:8080", "Local development server")
namespace CallCalendar;

model HealthResponse {
  status: "UP" | "DOWN";
}

@route("/actuator/health")
@get
op health(): HealthResponse;
