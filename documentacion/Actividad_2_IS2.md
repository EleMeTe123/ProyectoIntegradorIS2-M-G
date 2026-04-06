# Auditoría de Riesgos del Proyecto (Software)

## Analisi de Riesgo generado por ClickUp
### 1) Riesgos técnicos

| Riesgo | Probabilidad | Impacto |
|---|---|---|
| Modelado incompleto de “Materias” (núcleo del dominio) | Alta | Alto |
| Déficit de testing (calidad y regresiones) | Alta | Alto |
| Problemas de consistencia/performance con ORM (ActiveJDBC) + SQLite | Media | Medio-Alto |
| Seguridad y control de acceso insuficiente (auth, sesiones, permisos) | Media | Alto |
| Dependencia obligatoria de Internet | Media | Medio |
| Arquitectura cliente/servidor sin detalle de despliegue/operación || Media | Medio |
| Compatibilidad multi-navegador subestimada | Media | Medio |
| SparkJava (microframework) puede aumentar trabajo manual (validaciones, auth, manejo de errores) | Media | Medio |

---

### 2) Riesgos organizacionales

| Riesgo | Probabilidad | Impacto |
|---|---|---|
| Equipo mínimo (bus factor = 1) | Alta | Alto |
| Overhead o mala aplicación de Scrum en equipo muy pequeño (roles difusos, ceremonias) | Media | Medio |


---

### 3) Riesgos de planificación

| Riesgo | Probabilidad | Impacto |
|---|---|---|
| Plazo fijo de 3 meses con poco margen ante imprevistos | Alta | Alto |
| Scope creep o recorte de funcionalidades críticas | Alta | Alto |
| Backlog/issues difíciles de controlar (planificación débil) | Alta | Medio-Alto |
| Falta de criterios de aceptación y estrategia de QA | Alta | Alto |
| Subestimación del esfuerzo de modelo de datos y reglas del dominio | Media | Alto |

---

### 4) Riesgos humanos

| Riesgo | Probabilidad | Impacto |
|---|---|---|
| Sobrecarga y fatiga (2 personas cubren análisis, dev, QA, despliegue) | Alta | Alto |
| Conocimiento desigual del stack o prácticas (testing, patrones, seguridad) | Media | Medio-Alto |
| Bloqueos por dependencia mutua alta (si uno se atrasa, el otro también) | Media | Medio |
| Disponibilidad variable por contexto académico (exámenes, horarios) | Media | Alto |

---

## Analisis de Riesgo hecho por el equipo de trabajo
| Tipo de Riesgo | Descripcion                     | Probabilidad | Impacto |
|----------------|---------------------------------| --- |---------|
| Tecnico        | Falta de modelado de "Materias" | Alta | Alto |
| Tecnico        | Falta de testing | Alta | Alto    |
| Organizacional | tamaño del grupo | Alta | Alto |
| Organizacional | inexperiencia al dividir roles y tareas | Media | Alto |
| Planificacion  | Poco margen de tiempo | Alta | Alto |
| Planificacion  | inexperiencia en estimacion de deadlines | Alta | Alto |
| Riesgo Humano  | falta de compromiso | Media | Alto |
| Riesgo Humano  | falta de comunicacion | Media | Alta |
| Riesgo Humano  | sobrecarga | Media | Alta |

## Comparacion de Resultados

Los dos analisis de riesgos demostraron resultados similares.
Esto permite al grupo evidenciar los puntos criticos en el desarrollo del proyecto como 
tambien el enfoque que debemos tomar.

El analisis tecnico revelo algunas problematicas importantes a las 
que debemos tomar en consideracion.

Y en terminos de organizacion, planificacion y humano, esta muy presente el hecho
de que somos 2 integrantes y que la organizacion, comunicacion y planificacion
seran puntos cruciales a la hora de realizar este proyecto.
