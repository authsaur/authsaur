import { authnHandlers, authnMFAHandlers, authnSocialHandlers } from '@/tools';
import { ArrowLeftOutlined } from '@ant-design/icons';
import { ProList } from '@ant-design/pro-components';
import { PageContainer } from '@ant-design/pro-layout';
import { Breadcrumb, Card, List, Space, Tag, Tooltip } from 'antd';
import { Link, history, useRequest } from 'umi';
import { queryFakeList } from '../list/service';


const uniqKey = ["pass_word", 'dingtalk', "mfa_otp"];
// const uniqKey = ["pass_word"];


const CardList = () => {
  const { data, loading } = useRequest(() => {
    return queryFakeList({
      count: 5,
    });
  });

  const list = data?.list || [];
  const types = list?.map((item) => (item.type)) || [];
  console.log(types)
  return (
    <PageContainer
      loading={loading}
      content={<Breadcrumb>
        <Link to="/authentication/list">
          <Space>
            <ArrowLeftOutlined />
            <span>返回</span>
          </Space>
        </Link>
      </Breadcrumb>}
    >

      <Card title="基础认证器" >
        <ProList
          // bordered
          // grid={{ gutter: 0, column: 1 }}
          // headerTitle="企业认证器"
          grid={{ gutter: 16, xs: 1, sm: 1, md: 2, lg: 3, xl: 3, xxl: 4 }}
          dataSource={authnHandlers}
          // split={true}
          // itemLayout="horizontal"
          renderItem={item => (
            <List.Item>
              <Card
                // title={item.label}
                onClick={() => { !item.disabled && !(types.includes(item.key) && uniqKey.includes(item.key)) && history.push(`/authentication/${item.key}`) }}
                bordered={false}
                hoverable={!item.disabled && !(types.includes(item.key) && uniqKey.includes(item.key))}
                style={{ backgroundColor: item.disabled ? '#fff' : '#f7f8fa' }}
              >
                <List.Item.Meta
                  avatar={item.icon}
                  title={<Space>{item.label}{item.disabled ? <Tag>敬请期待</Tag> : ((types.includes(item.key) && uniqKey.includes(item.key)) ?
                    <Tag color='green'>已添加</Tag> : '')}</Space>}
                  description={
                    <Tooltip title={item.desc}>
                      <div style={{ lineClamp: 2, WebkitLineClamp: 2, boxOrient: 'vertical', WebkitBoxOrient: 'vertical', overflow: 'hidden', display: '-webkit-box', }}>
                        {item.desc}
                      </div>
                    </Tooltip>
                  }
                >
                </List.Item.Meta>
              </Card>
            </List.Item>
          )
          }
        />
      </Card>

      <Card title="双因素认证器"
        // extra="双因认证要求登录的不仅仅是一个密码，从而为您的帐户增加了一层额外的安全性。"
        style={{ marginTop: '16px' }}>
        <ProList
          // headerTitle="双因素认证器"
          // grid={{ gutter: 20, column: 2 }}
          grid={{ gutter: 16, xs: 1, sm: 1, md: 2, lg: 3, xl: 3, xxl: 4 }}
          dataSource={authnMFAHandlers}
          split={true}
          itemLayout="horizontal"
          renderItem={item => (
            <List.Item>
              <Card
                // title={item.label}
                onClick={() => { !item.disabled && !(types.includes(item.key) && uniqKey.includes(item.key)) && history.push(`/authentication/${item.key}`) }}
                bordered={false}
                hoverable={!item.disabled && !(types.includes(item.key) && uniqKey.includes(item.key))}
                style={{ backgroundColor: item.disabled ? '#fff' : '#f7f8fa' }}
              >
                <List.Item.Meta
                  avatar={item.icon}
                  title={<Space>{item.label}{item.disabled ? <Tag>敬请期待</Tag> : ((types.includes(item.key) && uniqKey.includes(item.key)) ?
                    <Tag color='green'>已添加</Tag> : '')}</Space>}
                  description={
                    <Tooltip title={item.desc}>
                      <div style={{ lineClamp: 2, WebkitLineClamp: 2, boxOrient: 'vertical', WebkitBoxOrient: 'vertical', overflow: 'hidden', display: '-webkit-box', }}>
                        {item.desc}
                      </div>
                    </Tooltip>
                  }
                >
                </List.Item.Meta>
              </Card>
            </List.Item>
          )
          }
        />
      </Card>
      <Card title="社会化认证器" style={{ marginTop: '16px' }}>
        <ProList
          // bordered
          // grid={{ gutter: 0, column: 1 }}
          grid={{ gutter: 16, xs: 1, sm: 1, md: 2, lg: 3, xl: 3, xxl: 4 }}
          dataSource={authnSocialHandlers}
          split={true}
          itemLayout="horizontal"
          renderItem={item => (
            <List.Item>
              <Card
                // title={item.label}
                onClick={() => { !item.disabled && !(types.includes(item.key) && uniqKey.includes(item.key)) && history.push(`/authentication/${item.key}`) }}
                bordered={false}
                hoverable={!item.disabled && !(types.includes(item.key) && uniqKey.includes(item.key))}
                style={{ backgroundColor: item.disabled ? '#fff' : '#f7f8fa' }}
              >
                <List.Item.Meta
                  avatar={item.icon}
                  title={<Space>{item.label}{item.disabled ? <Tag>敬请期待</Tag> : ((types.includes(item.key) && uniqKey.includes(item.key)) ?
                    <Tag color='green'>已添加</Tag> : '')}</Space>}
                  description={
                    <Tooltip title={item.desc}>
                      <div style={{ lineClamp: 2, WebkitLineClamp: 2, boxOrient: 'vertical', WebkitBoxOrient: 'vertical', overflow: 'hidden', display: '-webkit-box', }}>
                        {item.desc}
                      </div>
                    </Tooltip>
                  }
                >
                </List.Item.Meta>
              </Card>
            </List.Item>
          )
          }
        />
      </Card>
    </PageContainer>
  );
};

export default CardList;
