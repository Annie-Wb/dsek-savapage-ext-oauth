# D-sek savapage-ext-oauth

## OAuth Client Plug-in for Authentik (auth.dsek.se)

### License

This module is part of the SavaPage project <https://www.savapage.org>,
copyright (c) 2020 Datraverse B.V. and licensed under the
[GNU Affero General Public License (AGPL)](https://www.gnu.org/licenses/agpl.html)
version 3, or (at your option) any later version.

### Usage
1. Install using `make package`

2. Copy plug-in library to server environment:

    `sudo target/savapage-ext-oauth.jar /opt/savapage/server/ext/lib`
    
3. Copy plug-in property file and fill in with required ID
  
    `sudo cp savapage-ext-oauth-openid.properties /opt/savapage/server/ext`

4. Set ownership and restrict permissions, since properties file contains confidential information:
    
    `sudo chown savapage:savapage /opt/savapage/server/ext/savapage-ext-oauth-*.properties`
    `sudo chmod 600 /opt/savapage/server/ext/savapage-ext-oauth-*.properties`
       
5. Restart SavaPage with `systemctl restart savapage` 
