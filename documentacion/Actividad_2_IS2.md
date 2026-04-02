# Informe de Auditoría de Riesgos Idebtificada por IA LLM: Sistema de Gestión Universitaria

## 1. Riesgos Técnicos
| Riesgo | Probabilidad | Impacto | Justificación |
| :--- | :--- | :--- | :--- |
| **Limitación de Concurrencia (SQLite)** | **Alta** | **Medio** | SQLite no está diseñado para múltiples escrituras simultáneas, lo que puede fallar si muchos usuarios acceden a la vez. |
| **Carencia de Definición de Entorno** | **Alta** | **Alto** | La sección de "Restricciones Técnicas" está vacía, lo que implica incertidumbre sobre el despliegue final. |
| **Complejidad en la Lógica de Vista** | Media | Bajo | Mustache prohíbe lógica en las plantillas; esto obliga a saturar el backend de Java con lógica de presentación. |
| **Brecha en Lógica de Autorización** | Media | **Alto** | Aunque se usa BCrypt para contraseñas, falta definir el control de acceso para que un "Alumno" no acceda a funciones de "Admin". |

## 2. Riesgos Organizacionales
| Riesgo | Probabilidad | Impacto | Justificación |
| :--- | :--- | :--- | :--- |
| **Sobrecarga Metodológica (SCRUM)** | **Alta** | Bajo | Aplicar SCRUM completo en un equipo de solo 2 personas puede generar más burocracia que desarrollo efectivo. |
| **Aislamiento del Cliente** | Media | **Alto** | No hay registro de feedback o cambios de alcance, lo que sugiere falta de validación externa. |
| **Ambigüedad de Roles Internos** | Media | Medio | No se define quién actúa como Product Owner o responsable de calidad (QA) dentro del equipo. |

## 3. Riesgos de Planificación
| Riesgo | Probabilidad | Impacto | Justificación |
| :--- | :--- | :--- | :--- |
| **Alcance No Acotado (Scope Creep)** | **Alta** | **Crítico** | Las "Consultas académicas" no tienen un límite definido, lo que puede expandir el trabajo indefinidamente. |
| **Cronograma Optimista** | Media | **Alto** | 3 meses para un sistema de gestión completo con roles únicos es un plazo muy ajustado para solo 2 personas. |
| **Dependencia de Integración** | Baja | Medio | La centralización de datos suele requerir procesos de limpieza y migración no contemplados inicialmente. |

## 4. Riesgos Humanos
| Riesgo | Probabilidad | Impacto | Justificación |
| :--- | :--- | :--- | :--- |
| **Factor de Camión (Bus Factor)** | Media | **Crítico** | Con solo 2 integrantes, cualquier imprevisto personal de uno detiene el 50% de la capacidad de desarrollo. |
| **Sesgo de Especialización** | Media | Medio | Si ambos se enfocan en la misma área (ej. Backend), la interfaz de usuario y la experiencia de uso sufrirán. |
| **Fatiga por Carga de Trabajo** | Baja | Medio | El cumplimiento del plazo estricto podría exigir jornadas excesivas dada la magnitud del sistema. |
