# Sistema de monitoreo SNMP con Java, SNMP4J y Swing

Este proyecto desarrolla un programa en Java que permite:
- revisar la configuración de SNMP en Windows,
- consultar equipos mediante SNMP con la librería SNMP4J,
- detectar fallas y mostrarlas exclusivamente en una interfaz gráfica,
- actualizar la información periódicamente y evitar información innecesaria cuando todo funciona bien.

## Objetivo

El programa busca monitorizar equipos conectados a la misma red usando SNMP. Se puede utilizar para detectar:
- equipos que no responden,
- fallos de comunicación SNMP,
- ausencia de servicio SNMP o configuración incorrecta,
- valores de carga de CPU o recursos que excedan un umbral si la información está disponible.

No se muestra información “normal” del equipo; solo se presentan fallas y alertas, y cuando no existen alertas la interfaz muestra:

No se detectaron fallas

## Diferencia entre habilitar SNMP en Windows y desarrollar la app Java

### 1) Habilitar SNMP en Windows

La activación del servicio SNMP del agente en una máquina Windows requiere:
- permisos de administrador,
- acceso a la configuración del sistema,
- normalmente, pasos manuales o ejecución de comandos PowerShell/administrador.

Java puede intentar automatizar esa configuración, pero no puede reemplazar la política del sistema operativo ni la elevación de permisos.

### 2) La aplicación Java

La aplicación Java simplemente se comunica con un equipo usando SNMP, por ejemplo:
- consulta la información del sistema,
- verifica si responde SNMP,
- interpreta los valores recibidos,
- muestra alertas si algo falla.

Los pasos de instalación o habilitación de SNMP en el equipo monitoreado son una tarea del sistema operativo, no del código Java.

## Requisitos

- Java 17+
- Maven 3.9+
- VS Code
- Conexión de red con equipos compatibles con SNMPv2c
- Acceso administrador para activar SNMP en Windows y ajustar permisos si es necesario

## Dependencias

- SNMP4J: `org.snmp4j:snmp4j:3.7.8`
- JUnit 5 para pruebas de lógica

## Instalación de dependencias

Desde la raíz del proyecto:

```bash
mvn clean install
```

## Ejecución

### Opción 1: desde Maven

```bash
mvn exec:java -Dexec.mainClass=com.example.snmpmonitor.Main
```

### Opción 2: jar ejecutable

```bash
mvn clean package
java -jar target/snmp-monitoring-system-1.0.0.jar
```

## Cómo usar la aplicación

1. Abrir la aplicación.
2. Agregar un equipo por IP o nombre del host.
3. Definir la comunidad SNMP, por ejemplo `public`.
4. Pulsar “Iniciar monitoreo”.
5. La aplicación revisa cada equipo cada cierto tiempo.
6. Solo aparecen los fallos detectados.

## Configuración manual de SNMP en Windows

> Estas acciones requieren permisos de administrador.

### Activar SNMP en Windows 10/11

1. Abrir PowerShell como administrador.
2. Ejecutar:

```powershell
Get-WindowsCapability -Online -Name SNMP* 
```

Si está disponible, se puede intentar instalar el servicio de forma manual con:

```powershell
Enable-WindowsOptionalFeature -Online -FeatureName SNMP -All
```

O bien, si el sistema usa una versión donde el servicio está disponible como característica del sistema:

```powershell
Get-WindowsOptionalFeature -Online | Where-Object {$_.FeatureName -like "*SNMP*"}
```

### Verificar servicio

```powershell
Get-Service SNMP* | Format-Table Name, Status, StartType
```

### Iniciar el servicio

```powershell
net start SNMP
```

### Configurar arranque automático

```powershell
sc config SNMP start= auto
```

### Ajustar permisos de seguridad (si la política del sistema lo requiere)

```powershell
reg query "HKLM\SYSTEM\CurrentControlSet\Services\SNMP\Parameters"
```

Esto es un ejemplo de configuración del servicio SNMP; la preparación exacta puede variar según la edición de Windows y la política del equipo.

## Automatización desde Java

La aplicación incluye una clase de ayuda llamada `WindowsSnmpSetupHelper` que puede:
- detectar si el sistema operativo es Windows,
- comprobar si el servicio SNMP de Windows está presente,
- preparar instrucciones para ejecutar los comandos con permisos de administrador,
- intentar lanzar PowerShell con elevación si el usuario tiene permisos.

Sin embargo, Java no puede forzar la elevación de permisos en todos los equipos. Si la app se ejecuta sin permisos de administrador, el resultado será un aviso claro indicando que la habilitación manual o administrativa es necesaria.

## Seguridad

- No se almacenan credenciales SNMP en texto plano en la interfaz principal.
- La comunidad SNMP se usa solo en memoria durante la sesión.
- La aplicación no expone contraseñas o credenciales en la salida de consola.
- Se recomienda usar SNMPv2c solo en redes controladas, con comunidades restrictivas y restricciones de firewall.

## Registro de fallas detectadas

La aplicación muestra únicamente los eventos que cumplen estos criterios:

- El equipo no responde a la consulta SNMP.
- SNMP no está habilitado o no responde.
- El valor de CPU o recurso supera el umbral configurado y la información está disponible.

Cuando todo está correcto, la app presenta:

```text
No se detectaron fallas
```

## Pruebas recomendadas

1. Verificar un host local con SNMP habilitado y comunidad correcta.
2. Probar una IP inexistente o un equipo apagado.
3. Probar una IP con SNMP deshabilitado.
4. Probar un host con comunidad incorrecta.
5. Probar un host con valores de CPU por encima del límite establecido, si el agente SNMP expone ese dato.
6. Confirmar que la interfaz solo muestre alertas y no datos “normales” innecesarios.

## Pruebas automáticas incluida

Se incluyen pruebas JUnit para verificar la lógica central de detección de fallas:

```bash
mvn test
```

## Estructura del proyecto

```text
src/
  main/
    java/
      com/example/snmpmonitor/
        Main.java
        HostConfig.java
        AlertEvent.java
        SnmpCheckResult.java
        SnmpClientService.java
        AlertEvaluator.java
        MonitoringService.java
        WindowsSnmpSetupHelper.java
        MonitoringWindow.java
  test/
    java/
      com/example/snmpmonitor/
        AlertEvaluatorTest.java
```

## Nota importante

La aplicación no inventa fallas. Si un valor no está disponible, no se reporta. Si un equipo responde normalmente, no se presenta como fallo. De ese modo, la lógica cumple la condición de no marcar alertas sin comprobación.
