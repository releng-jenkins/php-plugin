package com.ganz.jenkins.php.BuildStep;
f=namespace(lib.FormTagLib)


    f.entry(title:_("Version")) {
        select(class:"setting-input",name:"_.php") {
        	option(value:"(Default)", _("Default"))
            descriptor.installations.each {
            	f.option(selected:it.name==instance?.installation?.name, value:it.name, it.name)
            }
        }
    }
