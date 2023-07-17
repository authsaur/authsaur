export default [
  {
    path: '/user',
    layout: false,
    routes: [
      {
        name: 'login',
        path: '/user/login',
        component: './user/Login',
      },
      {
        path: '/user/cas',
        layout: false,
        component: './user/CAS',
      },
      {
        component: './404',
      },
    ],
  },
  // {
  //   path: '/welcome',
  //   name: 'welcome',
  //   icon: 'smile',
  //   component: './Welcome',
  // },
  {
    path: '/service',
    name: 'service',
    icon: 'AppstoreOutlined',
    // access: 'canAdmin',
    hideChildrenInMenu: true,
    routes: [
      {
        path: '/service',
        redirect: '/service/list',
      },
      {
        name: 'list',
        path: '/service/list',
        component: './Service/list',
      },
      {
        name: 'method',
        path: '/service/method',
        component: './Service/method',
      },
      {
        name: 'protocol',
        path: '/service/protocol',
        component: './Service/protocol',
      },
      {
        name: 'add',
        path: '/service/:type/add',

        component: './Service/add',
      },
      {
        name: 'add',
        path: '/service/add/zentao',
        component: './Service/add/zentao',
      },
      {
        name: 'edit',
        path: '/service/edit/:id/zentao',
        component: './Service/edit/zentao',
      },
      {
        name: 'add',
        path: '/service/add/Gitlab',
        component: './Service/add/Gitlab',
      },
      {
        name: 'add',
        path: '/service/add/authsaur-admin',
        component: './Service/add/AuthsaurAdmin',
      },
      {
        name: 'add',
        path: '/service/add/CoDesign',
        component: './Service/add/CoDesign',
      },
      {
        name: 'edit',
        path: '/service/edit/:id/CoDesign',
        component: './Service/edit/CoDesign',
      },
      {
        name: 'add',
        path: '/service/add/Jenkins',
        component: './Service/add/Jenkins',
      },
      {
        name: 'edit',
        path: '/service/edit/:id/Jenkins',
        component: './Service/edit/Jenkins',
      },
      {
        name: 'add',
        path: '/service/add/Sentry',
        component: './Service/add/Sentry',
      },
      {
        name: 'edit',
        path: '/service/edit/:id/Sentry',
        component: './Service/edit/Sentry',
      },
      {
        name: 'add',
        path: '/service/add/Sonarqube',
        component: './Service/add/Sonarqube',
      },
      {
        name: 'edit',
        path: '/service/edit/:id/Sonarqube',
        component: './Service/edit/Sonarqube',
      },
      {
        name: 'add',
        path: '/service/add/Grafana',
        component: './Service/add/Grafana',
      },
      {
        name: 'edit',
        path: '/service/edit/:id/Grafana',
        component: './Service/edit/Grafana',
      },
      {
        name: 'add',
        path: '/service/add/JumpServer',
        component: './Service/add/JumpServer',
      },
      {
        name: 'edit',
        path: '/service/edit/:id/JumpServer',
        component: './Service/edit/JumpServer',
      },
      {
        name: 'add',
        path: '/service/add/Gerrit',
        component: './Service/add/Gerrit',
      },
      {
        name: 'edit',
        path: '/service/edit/:id/Gerrit',
        component: './Service/edit/Gerrit',
      },
      {
        name: 'add',
        path: '/service/add/Jira',
        component: './Service/add/Jira',
      },
      {
        name: 'edit',
        path: '/service/edit/:id/Jira',
        component: './Service/edit/Jira',
      },
      {
        name: 'add',
        path: '/service/add/Confluence',
        component: './Service/add/Confluence',
      },
      {
        name: 'edit',
        path: '/service/edit/:id/Confluence',
        component: './Service/edit/Confluence',
      },
      {
        name: 'add',
        path: '/service/add/Seafile',
        component: './Service/add/Seafile',
      },
      {
        name: 'edit',
        path: '/service/edit/:id/Seafile',
        component: './Service/edit/Seafile',
      },
      {
        name: 'edit',
        path: '/service/edit/:id/authsaur-admin',
        component: './Service/edit/AuthsaurAdmin',
      },
      {
        name: 'edit',
        path: '/service/edit/:id/Gitlab',
        component: './Service/edit/Gitlab',
      },
      {
        name: 'edit',
        path: '/service/edit/:id/CAS',
        component: './Service/edit',
      },
      {
        name: 'edit',
        path: '/service/edit/:id/OAuth2',
        component: './Service/edit',
      },
      {
        name: 'edit',
        path: '/service/edit/:id/SAML',
        component: './Service/edit',
      },
      {
        name: 'edit',
        path: '/service/edit/:id/Oidc',
        component: './Service/edit',
      },
    ],
  },
  {
    path: '/authentication',
    name: 'authentication',
    icon: 'KeyOutlined',
    // flatMenu: true,
    hideChildrenInMenu: true,
    routes: [
      {
        path: '/authentication',
        redirect: '/authentication/list',
      },
      {
        name: 'list',
        path: '/authentication/list',
        component: './Auth/list',
      },
      {
        name: 'method',
        path: '/authentication/method',
        component: './Auth/method',
      },
      {
        name: 'add',
        path: '/authentication/ldap',
        component: './Auth/add',
      },
      {
        name: 'edit',
        path: '/authentication/edit/:id/ldap',
        component: './Auth/edit',
      },
      {
        name: 'add',
        path: '/authentication/radius',
        component: './Auth/add/radius',
      },
      {
        name: 'edit',
        path: '/authentication/edit/:id/radius',
        component: './Auth/edit/radius',
      },
      {
        name: 'edit',
        path: '/authentication/edit/:id/dingtalk',

        component: './Auth/edit/dingtalk',
      },
      {
        name: 'add',
        path: '/authentication/dingtalk',
        component: './Auth/add/dingtalk',
      },
      {
        name: 'add',
        path: '/authentication/mfa_otp',
        component: './Auth/add/otp',
      },
      {
        name: 'edit',
        path: '/authentication/edit/:id/mfa_otp',
        component: './Auth/edit/otp',
      },
    ],
  },
  {
    path: '/member',
    name: 'user',
    icon: 'UserOutlined',
    hideChildrenInMenu: true,
    routes: [
      {
        path: '/member',
        redirect: '/member/view',
      },
      {
        name: 'view',
        path: '/member/view',
        component: './Account/view',
      },
      {
        name: 'list',
        path: '/member/:source/list',
        component: './Account/list',
      },
    ],
  },

  {
    path: '/authority',
    name: 'authority',
    icon: 'IdcardOutlined',
    hideChildrenInMenu: true,
    routes: [
      {
        path: '/authority',
        redirect: '/authority/list',
      },
      {
        name: 'list',
        path: '/authority/list',
        component: './Authority/list',
      },
    ],
  },

  {
    path: '/audit',
    name: 'audit',
    icon: 'FileSearchOutlined',
    hideChildrenInMenu: true,
    routes: [
      {
        path: '/audit',
        redirect: '/audit/user',
      },
      {
        name: 'core',
        path: '/audit/user',
        component: './Audit/list',
      },
      {
        name: 'base',
        path: '/audit/console',
        component: './Audit/list',
      },
    ],
  },
  // {
  //   path: '/manager',
  //   name: 'manager',
  //   icon: 'AppstoreOutlined',
  //   routes: [
  //     {
  //       path: '/manager',
  //       redirect: '/manager/core',
  //     },
  //     {
  //       path: '/manager/core',
  //       name: 'core'
  //     },
  //     {
  //       path: '/manager/base',
  //       name: 'base'
  //     },
  //     {
  //       component: './404',
  //     },
  //   ],
  // },
  {
    path: '/brand',
    name: 'brand',
    icon: 'BgColorsOutlined',
    hideChildrenInMenu: true,
    routes: [
      {
        path: '/brand',
        redirect: '/brand/core',
      },
      {
        path: '/brand/core',
        name: 'core',
        component: './Branding/list',
      },

     
    ],
  },
  {
    path: '/setting',
    name: 'setting',
    icon: 'SettingOutlined',
    hideChildrenInMenu: true,
    routes: [
      {
        path: '/setting',
        redirect: '/setting/config',
      },
      {
        path: '/setting/config',
        name: 'config',
        component: './Setting/list',
      },
      {
        component: './404',
      },
    ],
  },
  {
    path: '/',
    redirect: '/service',
  },
  {
    component: './404',
  },
];
