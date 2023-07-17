
import type { Request, Response } from 'express';


function getFakeList(req: Request, res: Response) {
  return res.json({
    data: {
      "list": [
        {
          "name": "本地身份源认证",
          "id": "-1024",
          "state": "ACTIVE",
          "relatedWithPhone": false,
          "type": "pass_word",
          "config": {},
          "relatedWithMail": false
        },
        {
          "name": "LDAP Authn",
          "id": 5.0,
          "state": "ACTIVE1",
          "type": 'ldap',
          "config": {
            "baseDn": "ou=users,dc=tiger,dc=com",
            "bindDn": "cn=admin,dc=tiger,dc=com",
            "bindCredential": "hy123!!!",
            "ldapUrl": "ldap://116.205.137.175:389",
            "useStartTls": false,
            "searchFilter": "(&(objectClass=top)(uid={user}))",
            "principalAttributeId": "uid",
            "principalAttributeList": [
              "cn",
              "name",
              "displayName",
              "mail",
              "telephoneNumber"
            ],
            "emailAttribute": "mail",
            "phoneAttribute": "telephoneNumber",
            "nameAttribute": "cn"
          }
        },
        {
          "name": "Dingtalk",
          "id": 7,
          "state": "ACTIVE",
          "type": 'dingtalk',
          "relatedAuthnId": 5,
          "config": {
            "cropId": "ding7699107c170fa706bc961a6cb783455b",
            "clientId": "dingsbj8ib7xq1fbiflt",
            "secret": "ZjN55NEpl7zTGuOGCotM1BUYu9ZPbTh8-u17eUOT6fN_cqmpjh03t3xKLuTEBfMx"
          },
        }
      ]
    },
  });
}

export default {
  'GET  /api/authenticator': getFakeList,
};
