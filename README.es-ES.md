

# jobdeploy

[![Maven](https://github.com/all3n/jobdeploy/actions/workflows/maven.yml/badge.svg)](https://github.com/all3n/jobdeploy/actions/workflows/maven.yml)
[![Docker](https://github.com/all3n/jobdeploy/actions/workflows/docker-image.yml/badge.svg)](https://hub.docker.com/r/jobdeploy/jobdeploy/tags)
[![Version](https://img.shields.io/github/v/release/all3n/jobdeploy?style=social)](https://github.com/all3n/jobdeploy/releases)

## ¿Qué es Jobdeploy?
jobdeploy es una herramienta CLI de automatización implementada completamente en Java para realizar despliegues a través del protocolo SSH. Se configura con JSON y cuenta con pocas dependencias.

## Instalación [recomendada]
reemplaza JD_HOME con tu ruta de despliegue

```
JD_HOME=$HOME/opt/jobdeploy && mkdir -p $JD_HOME/bin && cd $JD_HOME/bin && wget -O deploy https://raw.githubusercontent.com/all3n/jobdeploy/master/script/deploy && chmod +x ./deploy && ./deploy --update
```
## Instalación [desde código fuente]

```
git clone https://github.com/all3n/jobdeploy.git
cd jobdeploy
./install $HOME/opt/jobdeploy
```


## Contenido
* [Referencia de Configuración](docs/config-reference.md)
* [Configuración de Etapas](docs/stages.md)
* [SCM git/svn](docs/samples.md)
* [Cómo Desplegar](docs/how-to-deploy.md)
* [Tareas](docs/tasks.md)
* [Estrategia de Despliegue](docs/strategy.md)
* [Modo de Despliegue](docs/deploy-mode.md)
* [Autenticación](docs/authentication.md)
* [SharedAssets](docs/SharedAssets.md)
* [Plantillas](docs/templates.md)
* [Notificaciones](docs/notify.md)
* [Preguntas Frecuentes](docs/faq.md)

## Soporte
* [Azkaban](docs/azkaban.md)
* [hooks](docs/hooks.md)
* [Proxy](docs/proxy.md)
* [Compatibilidad con SO](docs/os.md)
* [docker](docs/docker.md)
* [CI](docs/ci.md)
* [Extensiones](docs/extension.md)
* [GroovyScript](docs/groovyscript.md)


## Dependencias de Ejecución
* maven
* java  1.8

## Actualización
```
deploy --update
```
## [Ejemplos](https://github.com/all3n/jobdeploy/tree/master/samples)
