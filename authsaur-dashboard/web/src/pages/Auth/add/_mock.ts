
import type { Request, Response } from 'express';

export default {
  'POST  /api/authenticator/saveOrUpdateLdap': (_: Request, res: Response) => {
    res.send({ data: { message: 'Ok', id: 123 }, success: true });
  },
  'POST  /api/authenticator/saveOrUpdateDingTalk': (_: Request, res: Response) => {
    res.send({ data: { message: 'Ok' }, success: true });
  },
  'POST  /api/authenticator/saveOrUpdateOTP': (_: Request, res: Response) => {
    res.send({ data: { message: 'Ok' }, success: true });
  },
  'DELETE  /api/authenticator/:id': (_: Request, res: Response) => {
    res.send({ data: { message: 'Ok' }, success: true });
  },
  'PUT  /api/authenticator/:id': (_: Request, res: Response) => {
    res.send({ data: { message: 'Ok' }, success: true });
  },
  'GET  /api/usermanagement/search': (_: Request, res: Response) => {
    res.send({
      "code": 200,
      "data": {
        "list": [
          {
            "name": "Sayi",
            "id": "TGCgVYFMeiSEiE::7",
            "source": "7"
          }
        ]
      },
      "success": true,
      "msg": "SUCCESS"
    });
  },
  'GET /api/authenticator/:id': (req: Request, res: Response) => {
    res.send({
      "code": 200,
      "data": {
        "name": "LDAP Authn",
        "id": 5,
        "state": "ACTIVE",
        "relatedWithPhone": false,
        "type": 'ldap',
        "relatedAuthnId": "-1024",
        "config": {
          "baseDn": "ou=users,dc=tiger,dc=com",
          "bindDn": "cn=admin,dc=tiger,dc=com",
          "bindCredential": "hy123!!!",
          "ldapUrl": "ldap://116.205.137.175:389",
          "useStartTls": false,
          "searchFilterTemplate": "(&(objectClass=top)(%s={%s}))",
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
          "emailSearchFilter": "(&(objectClass=top)(mail={mail}))",
          "phoneAttribute": "telephoneNumber",
          "phoneSearchFilter": "(&(objectClass=top)(telephoneNumber={telephoneNumber}))",
          "nameAttribute": "cn",
          "cropId": "ding7699107c170fa706bc961a6cb783455b",
          "clientId": "dingsbj8ib7xq1fbiflt",
          "secret": "ZjN55NEpl7zTGuOGCotM1BUYu9ZPbTh8-u17eUOT6fN_cqmpjh03t3xKLuTEBfMx"
        },
        "relatedWithMail": true
      },
      "success": true,
      "msg": "SUCCESS"
    });
  }
};
