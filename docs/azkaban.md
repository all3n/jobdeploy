## Azkaban Job Upload
* Add a "jobs" directory under the project directory.

```
Folder structure:
--jobdir
    --jobs
        --project1
            a.job
        --project2
            b.job
```

* Execute "deploy [stage]:uploadJob" to automatically create a project based on the folder name and upload the workflow.
* If you want to include job uploads during the deploy process, you can add the "-uj" parameter.
* Configuration:
    * Global default configuration: deploy_config.properties, configure azkaban.url=https://azkaban.xxx.com
    * You can also configure a separate Azkaban address for each deploy in deploy.json using the azkaban_url property.
    * azkaban_user and azkaban_password are required when uploading jobs for a project.

```
Deploy code and upload Azkaban jobs simultaneously:
deploy stage -uj

Deploy jobs separately:
deploy stage:uploadJob
```
