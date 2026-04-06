# Actividad 1: Requirements

## Equipo de Trabajo

| Nombre   | Gonzalo Flores M. |
|:---------| :---------------- |
| **Rol** | Lider del Grupo |
| **Responsabilidad** | Completar el proyecto |
| **Contacto** | floresgo42@gmail.com |

| Nombre   | Mateo Echenique             |
|:---------|:----------------------------|
| **Rol** | Integrante del Grupo        |
| **Responsabilidad** | Completar el proyecto |
| **Contacto** | mateoechenique054@gmail.com |

- El equipo de trabajo establecio que el proyecto se manejara a traves de la plataforma Github.
- Se realizaran reuniones de trabajo presenciales 2 (dos) veces por semana (horarios del dictado de clases
practicas de la materia Ingenieria de Software II)
- Se realizara al menos una reunion virtual por medio de meet.
- Se opto por utilizar como metodologia de trabajo la metodologia agil SCRUM.

## Tamaño del equipo
- Numero de integrantes: 2

## Problema que se quiere resolver
El problema a resolver en la creacion de un sistema de gestion universitaria
que permita la centralizacion de informacion, las consultas academicas por parte de los
estudiantes y la gestion de las materias dictadas con sus profesores responsables.

## Usuarios del sistema
- Alumnos: Utilizan el sistema para consultar las mateias cursadas, cuales pueden cursar
y revisar notas de aprobacion.
- Profesores: Utilizan el sistema para consultar su cargo, las materias que dictan con
sus respectivos alumnos y parrelaizar la carga de notas.
- Administradores: son los responsables de la administracion del sistema. centralizan
la informacion, llevan el registro de los profesores y alumnos como tambien el registro
de las materias disponibles.

## Funcionalidades Principales
El sistema presenta las siguientes funcionalidades:

- Creacion de usuarios (profesores y estudiantes)
- Añadir profesores y asignarlos al dictado de materias
- Creacion de Materias

## Restricciones Tecnicas
Las restricciones tecnicas del proyecto son las siguientes:
- solo los usuarios deberian poder utilizar el sistema.
- tecnologias utilizadas(java, maven apache, sparkJava, ActiveJDBC, SQLite, BCript)
- el sistema depende de estar conectado a internet.
- el modelo de sistema es cliente/servidor
- debe utilizarse patrones de diseño para el desarrollo del proyecto.
- La interfaz sera una pagina web que debe pooder ejecutarse en cualquier navegador
- debe existir una conexion a la base de datos que sea eficiente.

 ## Tecnologías elegidas
Las tecnologias seleccionadas en el proyecto son:
- Java
- apache Maven
- SparkJava
- Mustache
- SQLite
- BCrypt

## Justificacion de eleccion de tecnologias
| Tecnologia       | Justificación                                                                                                                                                         |
|:-----------------|:----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Java**         | Permite construir una aplicación web con buen soporte de librerías en un entorno robusto y orientado a objetos                                                        |
| **Apache Maven** | Estándar en proyectos Java para manejar dependencias y el ciclo de build, ademas estandariza el entorno de desarrollo para tos los integrantes del equipo de produccion |
| **Spark**        | permite definir rutas get/post rápido, sin la complejidad de frameworks más pesados.                                                                                  |
| **Mustache**     | Permite renderizar HTML desde templates y separar la vista del código Java. Es simple y liviano, ideal para formularios y páginas básicas                             |
| **SQLite**       | Motor de base de datos relacional embebido que permite precindir de un servidor de base de datos aparte, lo cual facilita la portabilidad del proyecto                                                                                                                                                                       |
| **BCrypt**       | Se usa para hash seguro de contraseñas en vez de guardar texto plano.                                                                                                                                                                      |

## Plazo estimado

- El plazo estimado para este proyecto es la duracion de cursado de la materia Ingenieria
de Software II. Es decir 3 (tres) meses de duracion.

## Cambios de alcance ocurridos
Durante el proceso de desarrollo del proyecto uno de los principales cambios
de alcance es la imposibilidad de modelar la creacion de materias.
Otro cambio de alcance es la reduccion de tiempo destinada a la actividad de testing.

## Problemas encontrados
Dados los cabios de alcance mencionados anteriormente, uno de los principales problemas
es la falta de modelado de las materias y las funcionalidades relacionadas a la misma.
Otro de los problemas encontrados es el tamaño del equipo de trabajo, lo cual genera que no se
pueda planificar grandes cambio en el proyecto o reducir el numero de issues.

