# Frequently Asked Questions

1. Unable to create a folder
   * When executing scripts with different accounts, you may encounter the error "sudo: sorry, you must have a tty to run sudo." You can modify the sudo configuration to resolve this.
   * Edit the /etc/sudoers file.
   * Comment out the line "Default requiretty."
   * This line means that sudo requires a tty terminal by default. Commenting it out allows execution in the background.

2. Unable to use SSH key
   * Check the permissions of the .ssh directory.
   * Set the permissions of the authorized_keys file to 600.
   * Set the permissions of the .ssh directory to 700.

3. Permission denied
   * If the user account does not have permission to create the target deployment folder, you may encounter this error.
   * For example, if you are deploying as the user "deploy" and want to deploy to /target/a1, you can modify the permissions of the parent directory by running: chgrp deploy /target and chmod 775 /target.

4. Auth Fail
   * If you have changed your password, delete the line "password" in ~/.jobdeploy/my.properties and redeploy. This will prompt you to enter the password again for password authentication.

5. Deploy not updating
   * If you have redeployed but the files are not automatically updated when you SSH into the current link path, it's because you are still in the previous deployment path. Use the command "cd $PWD" to switch to the latest deployment path.
