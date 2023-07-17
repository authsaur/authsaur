import { coverString, marketServices } from '@/tools';
import { ArrowLeftOutlined, DingtalkOutlined } from '@ant-design/icons';
import { ProList } from '@ant-design/pro-components';
import { PageContainer } from '@ant-design/pro-layout';
import { Avatar, Breadcrumb, Card, Input, List, Space, Tag, Tooltip } from 'antd';
import Paragraph from 'antd/lib/typography/Paragraph';
import { useState } from 'react';
import { Link } from 'umi';

const { Search } = Input;

export type Member = {
  avatar: any;
  realName: string;
  nickName: string;
  email: string;
  outUserNo: string;
  phone: string;
  permission?: string[];
};

const items = Array.from(marketServices.values());


const CardList = () => {
  const [data, setData] = useState(items);
  return (
    <PageContainer
      content={<Breadcrumb>
        <Link to="/service/list">
          <Space>
            <ArrowLeftOutlined />
            <span>返回</span>
          </Space>
        </Link>
      </Breadcrumb>}
    >

      {/* <Card title="应用市场" ghost> */}
      <ProList
        // ghost
        grid={{ gutter: 16, xs: 1, sm: 1, md: 2, lg: 3, xl: 3, xxl: 4 }}
        dataSource={data}
        split={false}
        pagination={{
          defaultPageSize: 20,
          showSizeChanger: false,
        }}
        toolbar={{
          search: {
            onSearch: (value: string) => {
              if (value == '' || !value) {
                setData(items);
              } else {
                setData(items.filter((item) => coverString(value, item.title)));
              }
            },
            placeholder: "输入应用名搜索",
          },

        }}
        // itemLayout="horizontal"
        renderItem={item => (
          <List.Item key={item.key} style={{ padding: '0' }}>
            {!item.disabled ? <Link to={`/service/add/${item.key}`}>
              <Card
                // title={item.label}
                bordered={false}
                hoverable={!item.disabled}
                style={{ backgroundColor: '#f7f8fa' }}
              >
                <List.Item.Meta
                  avatar={item.avatar}
                  title={<Space>{item.title}<Tag color='blue'>{item.sso || 'SSO'}</Tag></Space>}
                  description={
                    <Tooltip title={item.description}>
                      <div style={{ lineClamp: 2, WebkitLineClamp: 2, boxOrient: 'vertical', WebkitBoxOrient: 'vertical', overflow: 'hidden', display: '-webkit-box', }}>
                        {item.description}
                      </div>
                    </Tooltip>
                  }
                />
              </Card>
            </Link> :<Card
                // title={item.label}
                bordered={false}
                hoverable={!item.disabled}
                style={{ cursor:'not-allowed', backgroundColor: '#f7f8fa' }}
              >
                <List.Item.Meta
                  avatar={item.avatar}
                  title={<Space>{item.title}<Tag>敬请期待</Tag></Space>}
                  description={
                    <Tooltip title={item.description}>
                      <div style={{ lineClamp: 2, WebkitLineClamp: 2, boxOrient: 'vertical', WebkitBoxOrient: 'vertical', overflow: 'hidden', display: '-webkit-box', }}>
                        {item.description}
                      </div>
                    </Tooltip>
                  }
                />
              </Card>}
          </List.Item>
        )
        }
      />
      {/* </Card> */}
    </PageContainer >
  );
};

export default CardList;
