This directory contains files for customizing the look and feel
of the submit server.  In particular, it allows the appearance and
UI text to be customized for your institution.

The web.xml file in here has some ant filter tags in it that will be 
replaced by ant upon being copied into WebRoot/WEB-INF.  Note that 
any changes to web.xml need to happen here rather than to 
WebRoot/WEB-INF/web.xml, since that file will be blown up every time.