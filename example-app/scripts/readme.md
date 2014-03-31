# Scripts

These scripts are used on QA or Release sites to help with deployment. It is suggested they are placed in "bin" under the user's
home directory you are using as a staging area.

If needed:

```
mkdir -p bin
# Copy the scripts into this directory
chmod 755 bin/*
vim .bash_profile
# Add the following (uncommented)
# PATH="$PATH:$HOME/bin"
# export PATH
```

On QA sites, we usually deploy from the install itself since it is quicker to do so.

In this case, you'll typically link the ROOT context to your deployed war directory. Something
like `ln -s $HOME/deployed-war /srv/tomcat/proteus-worker/webapps/ROOT`.

Example

```
[proteusaf@your-project-ec2 ~]$ ls -l /srv/tomcat/proteus-worker/webapps/
total 0
lrwxrwxrwx 1 proteusaf proteusaf 30 Mar 21 18:20 ROOT -> /var/lib/proteus/deployed-war/

```

If your deploying from a git repository on the EC2 instance, read the comments in the redeploy script.