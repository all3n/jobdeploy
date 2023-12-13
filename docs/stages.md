# Stage Deployment

Create a "deploy" folder and create different stage folders inside it, each with its own "deploy.json" file.
The "deploy.json" file under each stage will be merged with the project's "deploy.json".

Folder structure:
```
--jobdir
    deploy.json
    --deploy
        --sandbox
            deploy.json
        --prod
            deploy.json

```            
If you want to upload scripts for different stages, you can add an "@" symbol before the stage filename in the "upload" parameter (since 4.1.1).
For example: upload: ['@stageScript'], where "default" will be used if there is no corresponding stage folder.
The directory structure can be as follows:
```
--jobdir
    --stageScript
        --stage1
            - stage1 scripts
        --stage2
            - stage2 scripts
        --default
            - default scripts
```
If you want to use a specific stage file, you can specify it using the format: upload: ['stage1:stageScript'], separated by a colon to specify the stage script.


To deploy to prod: 

```
deploy prod [options]
```
