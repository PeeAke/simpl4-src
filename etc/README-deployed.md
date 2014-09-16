

[![alt text](https://raw.githubusercontent.com/ms123s/simpl4-deployed/master/etc/images/simpl4_logo.png "simpl4 logo")] (http://www.simpl4.org) installation
=================

####The best for testing
* Download [VirtualBox](https://www.virtualbox.org/wiki/Downloads) and this [Appliance] (http://simpl4.org/download/simpl4.ova) and install it. <br/>
	After the start, Wait 1-2 Minutes.<br/>
  Then you can login on [http://localhost:8080/sw/start.html](http://localhost:8080/sw/start.html) with admin/admin <br/>
	Hint: first time, click a second time after 10 seconds the login button.<br/>
	[now goto finish](#user-content-createId)<br/>
*	or use a<br/> 
	[Docker image] (https://registry.hub.docker.com/u/ms123s/simpl4/)  


####alternate, the  manually way(only few steps)

#####Requirement
1. java 1.7.0  
2. git( without "git", you can download a ziparchive on the right site of this page. )

----

#####Going to the destination directory
```bash
$ cd $ROOTDIR
```

#####Cloning this repository
```bash
$ git clone https://github.com/ms123s/simpl4-deployed.git simpl4
$ # or if you have downloaded the Ziparchive, unzip it.
```

#####change to repository directory
```bash
$ cd simpl4
```
----

#####Setup 
```bash
$ bin/setup.sh 
```
or in *windows*
```dos
c:\simpl4> bin\setup 
```
----

#####Starting the server
```bash
$ bin/start.sh start  
```
or in *windows*
```dos
c:\simpl4> bin\start
```

----

#####Now you can login on [http://localhost/sw/start.html](http://localhost/sw/start.html)  with admin/admin
![alt text](https://raw.githubusercontent.com/ms123s/simpl4-deployed/master/etc/images/login2-hc.png "simpl4 login2")  

----

#####<a name="createId"></a>Then create your first application
![alt text](https://raw.githubusercontent.com/ms123s/simpl4-deployed/master/etc/images/appcreate.png "firstapp")  

----

#####Congratulations,you  have Simpl4 succesfully installed, more on  [*simpl4*](http://simpl4.org)

