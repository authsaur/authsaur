import { DingtalkOutlined, EllipsisOutlined, FacebookFilled, GithubOutlined } from '@ant-design/icons';
import { Avatar, message } from 'antd';
import type { RcFile } from 'antd/es/upload/interface';


export const standardServices = new Map([
    ['OAuth2', {
        title: 'OAUTH2',
        key: 'OAuth2',
        avatar: <Avatar size="large" shape="square" src="/OAuth2.svg" />,
        description: 'OAuth 2.0 是行业标准的授权协议。',
    }],
    ['SAML', {
        title: 'SAML',
        key: 'SAML',
        avatar: <Avatar size="large" shape="square" src="/saml2.svg" />,
        description: 'SAML 是安全断言标记语言，是一个基于 XML 的开源标准数据格式。',
    }],
    ['CAS', {
        title: 'CAS',
        key: 'CAS',
        avatar: <Avatar size="large" shape="square" src="/cas.svg" />,
        description: 'CAS 是中央认证服务，一种独立开放指令协议。',
    }],
    ['Oidc', {
        title: 'OIDC',
        key: 'Oidc',
        avatar: <Avatar size="large" shape="square" src="/oidc.svg" />,
        description: 'OpenID Connect (OIDC) 扩展了 OAuth 2.0 授权协议。',
    }]
]);
export const marketServices = new Map([
    ['authsaur-admin', {
        title: 'Authsaur控制台',
        key: 'authsaur-admin',
        sso: 'CAS',
        avatar: <Avatar size="large" shape="square" src="/casing.png" />,
        description: 'Authsaur应用、用户、认证、审计、权限进行集中管理后台。',
    }],
    ['zentao', {
        title: '禅道',
        key: 'zentao',
        avatar: <Avatar size="large" shape="square" src="/zentao.svg" />,
        description: '禅道是专业的研发项目管理软件，完整覆盖研发项目核心流程。',
    }],
    ['Gitlab', {
        title: 'Gitlab',
        key: 'Gitlab',
        sso: 'SAML',
        avatar: <Avatar size="large" shape="square" src="/gitlab.svg" />,
        description: 'Gitlab一个基于Git实现的在线代码仓库托管软件。',
    }],
    ['Jenkins', {
        title: 'Jenkins',
        key: 'Jenkins',
        sso: 'SAML',
        avatar: <Avatar size="large" shape="square" src="/logo_Jenkins.svg" />,
        description: '构建伟大，无所不能。Jenkins 是开源 CI&CD 软件领导者。',
    }],
    ['CoDesign', {
        title: 'CoDesign',
        key: 'CoDesign',
        sso: 'CAS',
        avatar: <Avatar size="large" shape="square" src="/logo_codesign.svg" />,
        description: '腾讯自研的产品设计一站式协作平台，支持在线导入预览 Sketch 设计稿、自动生成设计标注切图，灵活调用图标库、素材库，支持多种插件上传，让产品设计更轻松高效。',
    }],
    ['Sentry', {
        title: 'Sentry',
        key: 'Sentry',
        sso: 'SAML',
        avatar: <Avatar size="large" shape="square" src="/logo_sentry.svg" />,
        description: 'Sentry 的应用程序监控平台可帮助每个开发人员诊断、修复和优化其代码的性能。',
    }],
    ['Sonarqube', {
        title: 'Sonarqube',
        key: 'Sonarqube',
        sso: 'SAML',
        avatar: <Avatar size="large" shape="square" src="/logo_sonarqube.svg" />,
        description: 'Sonarqube 可以通过数以千计的自动静态代码分析规则捕获应用程序中的 bug 和漏洞。',
    }],
    ['Grafana', {
        title: 'Grafana',
        key: 'Grafana',
        sso: 'OIDC',
        avatar: <Avatar size="large" shape="square" src="/logo_grafana.svg" />,
        description: 'Grafana 是所有数据库的开源分析和监控解决方案。',
    }],
    ['Jira', {
        title: 'Jira',
        key: 'Jira',
        sso: 'SAML',
        avatar: <Avatar size="large" shape="square" src="/logo_jira.svg" />,
        description: '在 Jira 中可以计划、跟踪和管理您的敏捷软件开发项目。自定义您的工作流、进行协作以及发布出色的软件。',
    }],
    ['JumpServer', {
        title: 'JumpServer',
        key: 'JumpServer',
        sso: 'CAS',
        avatar: <Avatar size="large" shape="square" src="/logo_jumpserver.svg" />,
        description: 'JumpServer 是全球首款开源的堡垒机，使用 GNU GPL v2.0 开源协议，是符合 4A 规范的运维安全审计系统。',
    }],
    ['Seafile', {
        title: 'Seafile',
        key: 'Seafile',
        sso: 'OAuth',
        avatar: <Avatar size="large" shape="square" src="/logo_seafile.svg" />,
        description: 'Seafile 是一款开源的企业云盘，注重可靠性和性能。支持 Windows, Mac, Linux, iOS, Android 平台。支持文件同步或者直接挂载到本地访问。',
    }],

    // ['Kibana', {
    //     title: 'Kibana',
    //     key: 'Kibana',
    //     disabled:true,
    //     avatar: <Avatar size="large" shape="square" src="/logo_kibana.svg" />,
    //     description: 'Kibana 是一个免费且开放的用户界面，能够让您对 Elasticsearch 数据进行可视化，并让您在 Elastic Stack 中进行导航。',
    // }],

    ['Confluence', {
        title: 'Confluence',
        key: 'Confluence',
        sso: 'SAML',
        avatar: <Avatar size="large" shape="square" src="/logo_confluence.svg" />,
        description: 'Confluence是一个企业级的Wiki软件，可用于在企业、部门、团队内部进行信息共享和协同编辑。',
    }],
    ['Gerrit', {
        title: 'Gerrit',
        key: 'Gerrit',
        sso: 'CAS',
        avatar: <Avatar size="large" shape="square" src="/logo_gerrit.svg" />,
        description: 'Gerrit，一种开放源代码的代码审查软件，使用网页界面。',
    }],
    // ['Zadig', {
    //     title: 'Zadig',
    //     key: 'Zadig',
    //     disabled:true,
    //     avatar: <Avatar size="large" shape="square" src="/logo_Zadig.svg" />,
    //     description: 'Zadig 是一款面向开发者设计的云原生持续交付 (Continuous Delivery) 产品，具备高可用 CI/CD 能力，提供云原生运行环境，支持开发者本地联调、微服务并行构建和部署、集成测试等。',
    // }],
    // ['Zabbix', {
    //     title: 'Zabbix',
    //     key: 'Zabbix',
    //     disabled:true,
    //     avatar: <Avatar size="large" shape="square" src="/logo_zabbix.svg" />,
    //     description: 'Zabbix 是一个成熟、易用的企业级开源监控解决方案，适用于百万级指标的网络监控和应用监控。',
    // }],
    ['More', {
        title: '更多应用',
        key: 'More',
        disabled:true,
        avatar:  <Avatar icon={<EllipsisOutlined />} size="large" shape="square"  />,
        description: '对SSO能力进行封装，更多第三方应用可以非常方便快捷的创建。',
    }],


]);

export const isStandardService = (type: string) => {
    return (['OAuth2', 'SAML', 'CAS', 'Oidc'].includes(type));

};

export const getServiceType = (item: any) => {
    let type = 'CAS';
    if (item['classType'].includes('Regex')) {
        type = 'CAS';
    } else if (item['classType'].includes('OAuth')) {
        type = 'OAuth2';
    } else if (item['classType'].includes('Saml')) {
        type = 'SAML';
    } else if (item['classType'].includes('Oidc')) {
        type = 'Oidc';
    }
    return type;
};

export const getProtocolLogo = (type: string) => {
    if (type == 'CAS') return '/cas.svg';
    if (type == 'SAML') return '/saml2.svg';
    if (type == 'OAuth2') return '/OAuth2.svg';
    if (type == 'Oidc') return '/oidc.svg';
    return "";
};

// authentication
export const authnMFAHandlers = [
    {
        label: 'OTP口令',
        key: 'mfa_otp',
        icon: <Avatar src="/otp.svg" shape='square' size='large' />,
        smallIcon: <Avatar src="/otp.svg" shape='square' size='small' />,
        desc: <>使用 OTP 一次性口令密码登录。</>,
    },
    {
        label: '电子邮箱验证',
        key: 'mfa_mail',
        disabled: true,
        icon: <Avatar src="/mfa_mail.svg" shape='square' size='large' />,
        smallIcon: <Avatar src="/mfa_mail.svg" shape='square' size='small' />,
        desc: <>使用邮件形式接收验证码认证登录。</>,
    },
    {
        label: '短信验证码',
        key: 'mfa_sms',
        disabled: true,
        icon: <Avatar src="/mfa_sms.svg" shape='square' size='large' />,
        smallIcon: <Avatar src="/mfa_sms.svg" shape='square' size='small' />,
        desc: <>使用短信形式接收验证码认证登录。</>,
    },
    {
        label: '指纹',
        key: 'mfa_fido',
        disabled: true,
        icon: <Avatar src="/mfa_fido.svg" shape='square' size='large' />,
        smallIcon: <Avatar src="/mfa_fido.svg" shape='square' size='small' />,
        desc: <>以识别用户指纹信息进行认证登录。</>,
    },
]
export const authnHandlers = [
    {
        label: '本地密码认证',
        key: 'pass_word',
        icon: <Avatar src="/password.svg" shape='square' size='large' />,
        smallIcon: <Avatar src="/password.svg" shape='square' size='small' />,
        desc: <>Authsaur系统内置，支持默认身份源内用户密码登录。</>,
    },
    {
        label: 'LDAP',
        key: 'ldap',
        icon: <Avatar src="/ldap.svg" shape='square' size='large' />,
        smallIcon: <Avatar src="/ldap.svg" shape='square' size='small' />,
        desc: <>LDAP 是一个开放的，中立的，工业标准的应用协议。</>,
    },

    {
        label: '钉钉',
        key: 'dingtalk',
        icon: <Avatar icon={<DingtalkOutlined />} size="large" shape="square" style={{ backgroundColor: 'var(--ant-primary-5)' }} />,
        smallIcon: <Avatar icon={<DingtalkOutlined />} size="small" shape="square" style={{ backgroundColor: 'var(--ant-primary-5)' }} />,
        desc: <>钉钉是阿里巴巴出品，专为全球企业组织打造的智能移动办公平台。</>,
    },
    {
        label: 'Radius',
        key: 'radius',
        icon: <Avatar src="/radius.svg" shape='square' size='large' />,
        smallIcon: <Avatar src="/radius.svg" shape='square' size='small' />,
        desc: <>RADIUS（远程用户拨号认证）协议是一种分布式的、客户端/服务器结构的信息交互协议。</>,
    },
    {
        label: '企业微信',
        key: 'weixin',
        disabled: true,
        icon: <Avatar src="/qyweixin.svg" shape='square' size='large' />,
        smallIcon: <Avatar src="/qyweixin.svg" shape='square' size='small' />,
        desc: <>企业微信是腾讯微信团队为企业打造的专业办公管理工具。</>,
    },
    {
        label: 'WeLink',
        key: 'WeLink',
        disabled: true,
        icon: <Avatar src="/WeLink.svg" shape='square' size='large' />,
        smallIcon: <Avatar src="/WeLink.svg" shape='square' size='small' />,
        desc: <>WeLink 是源自华为，更懂企业的全场景安全、智能、数字化协同办公平台。</>,
    },
    {
        label: '飞书',
        key: 'feishu',
        disabled: true,
        icon: <Avatar src="/feishu.svg" shape='square' size='large' />,
        smallIcon: <Avatar src="/feishu.svg" shape='square' size='small' />,
        desc: <>飞书是一站式先进企业协作与管理平台。</>,
    },
    {
        label: '电子邮箱验证',
        key: 'mail',
        disabled: true,
        icon: <Avatar src="/mfa_mail.svg" shape='square' size='large' />,
        smallIcon: <Avatar src="/mfa_mail.svg" shape='square' size='small' />,
        desc: <>使用邮件形式接收验证码认证登录。</>,
    },
    {
        label: '短信验证码',
        key: 'sms',
        disabled: true,
        icon: <Avatar src="/mfa_sms.svg" shape='square' size='large' />,
        smallIcon: <Avatar src="/mfa_sms.svg" shape='square' size='small' />,
        desc: <>使用短信形式接收验证码认证登录。</>,
    },
    {
        label: 'JWT TOKEN',
        key: 'jwt',
        disabled: true,
        icon: <Avatar src="/jwt.svg" shape='square' size='large' />,
        smallIcon: <Avatar src="/jwt.svg" shape='square' size='small' />,
        desc: <>JSON Web Tokens是一种开放的行业标准 RFC 7519 方法，用于在两方之间安全地表示声明。</>,
    }
];
export const authnSocialHandlers = [
    {
        label: '微信',
        key: 'social_weixin',
        disabled: true,
        icon: <Avatar src="/weixin.svg" shape='square' size='large' />,
        smallIcon: <Avatar src="/weixin.svg" shape='square' size='large' />,
        desc: <>微信是一款跨平台的通讯工具。支持单人、多人参与。</>,
    },
    {
        label: '微博',
        key: 'social_weibo',
        disabled: true,
        icon: <Avatar src="/weibo.svg" shape='square' size='large' />,
        smallIcon: <Avatar src="/weibo.svg" shape='square' size='large' />,
        desc: <>新浪微博是基于用户关系的社交媒体平台。</>,
    },
    {
        label: '百度',
        key: 'social_baidu',
        disabled: true,
        icon: <Avatar src="/baidu.svg" shape='square' size='large' />,
        smallIcon: <Avatar src="/baidu.svg" shape='square' size='large' />,
        desc: <>百度是拥有强大互联网基础的领先 AI 公司。</>,
    },
    {
        label: 'GitHub',
        key: 'social_github',
        disabled: true,
        icon: <Avatar icon={<GithubOutlined />} shape='square' size='large' style={{ backgroundColor: '#333' }} />,
        smallIcon: <Avatar icon={<GithubOutlined />} shape='square' size='large' style={{ backgroundColor: '#333' }} />,
        desc: <>GitHub 是一个面向开源及私有软件项目的托管平台。</>,
    },
    {
        label: 'Facebook',
        key: 'social_facebook',
        disabled: true,
        icon: <Avatar icon={<FacebookFilled />} shape='square' size='large' style={{ backgroundColor: 'var(--ant-primary-5)' }} />,
        smallIcon: <Avatar icon={<FacebookFilled />} shape='square' size='large' style={{ backgroundColor: 'var(--ant-primary-5)' }} />,
        desc: <>Facebook 是世界排名领先的照片分享站点。</>,
    },
    {
        label: 'Google',
        key: 'social_google',
        disabled: true,
        icon: <Avatar src="/google.svg" shape='square' size='large' />,
        smallIcon: <Avatar src="/google.svg" shape='square' size='large' />,
        desc: <>Google 是全球最大的搜索引擎公司。</>,
    },
];

export const getAuthnHandler = (key: string) => {
    const array = authnHandlers.filter(item => item.key == key);
    return array[0] || authnMFAHandlers.filter(item => item.key == key)[0] || authnSocialHandlers.filter(item => item.key == key)[0];
}

export const getBase64 = (img: RcFile, callback: (url: string) => void) => {
    const reader = new FileReader();
    reader.addEventListener('load', () => callback(reader.result as string));
    reader.readAsDataURL(img);
};

export const beforeUpload = (file: RcFile) => {
    const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png';
    if (!isJpgOrPng) {
        message.error('You can only upload JPG/PNG file!');
    }
    const isLt2M = file.size / 1024 / 1024 < 2;
    if (!isLt2M) {
        message.error('Image must smaller than 2MB!');
    }
    return isJpgOrPng && isLt2M;
};

/**
 * 忽略大小写判断字符串str是否包含subStr
 * @param subStr 子字符串
 * @param str 父字符串
 * @returns boolean
 */
export function coverString(subStr, str) {
    if (!subStr) return true;
    const reg = eval("/" + subStr + "/ig");
    return reg.test(str);
}


