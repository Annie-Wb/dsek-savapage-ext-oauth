# savapage-ext-oauth
    
OAuth Client Plug-in for:
  
* [Google](https://www.google.com/)
* [Microsoft Azure](https://login.microsoftonline.com/)
* [Smartschool](https://www.smartschool.be)

 
### License

This module is part of the SavaPage project <https://www.savapage.org>,
copyright (c) 2011-2019 Datraverse B.V. and licensed under the
[GNU Affero General Public License (AGPL)](https://www.gnu.org/licenses/agpl.html)
version 3, or (at your option) any later version.

### Join Efforts, Join our Community

SavaPage Software is produced by Community Partners and consumed by Community Members. If you want to modify and/or distribute our source code, please join us as Development Partner. By joining the [SavaPage Community](https://wiki.savapage.org) you can help build a truly Libre Print Management Solution. Please contact [info@savapage.org](mailto:info@savapage.org).

### Issue Management

[https://issues.savapage.org](https://issues.savapage.org)

### Usage

Copy plug-in library to server environment:

    $ sudo target/savapage-ext-oauth.jar /opt/savapage/server/ext/lib
    
Copy plug-in property file(s) to server environment:
    
    $ sudo cp savapage-ext-oauth-google.properties /opt/savapage/server/ext
    $ sudo cp savapage-ext-oauth-azure.properties /opt/savapage/server/ext
    $ sudo cp savapage-ext-oauth-smartschool.properties /opt/savapage/server/ext

Multiple Smartschool instances are permitted. See the annotated configuration keys in the `savapage-ext-oauth-smartschool.properties.template` file.

Set ownership and restrict permissions, since properties file contains confidential information:
    
    $ sudo chown savapage:savapage /opt/savapage/server/ext/savapage-ext-oauth-*.properties
    $ sudo chmod 600 /opt/savapage/server/ext/savapage-ext-oauth-*.properties

Edit `savapage-ext-oauth-*.properties` files to specify the necessary data.
       
Restart SavaPage.
