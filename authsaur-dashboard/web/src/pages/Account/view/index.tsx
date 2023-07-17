import { getAuthnHandler } from '@/tools';
import {
  InfoCircleTwoTone,
  UserOutlined
} from '@ant-design/icons';
import { ProList } from '@ant-design/pro-components';
import { PageContainer } from '@ant-design/pro-layout';
import { Card, List, Space } from 'antd';
import React from 'react';
import { Link, useRequest } from 'umi';
import { queryFakeDirectoryList } from './service';

const App: React.FC = () => {
  const { data, loading } = useRequest(() => {
    return queryFakeDirectoryList({
      count: 5,
    });
  });

  const list = (data?.list || [])
    .map((item) => {
      if (item?.related) {
        const relatedName = (data?.list || []).filter((i) => i.source == item?.related)[0].name;
        return {
          ...item,
          relatedName,
        };
      }
      return item;
    })
    .sort((a, b) => {
      if (a.source == '-1024') return -100;
      if (b.source == '-1024') return 100;
      if (a.related && b.related) return 1;
      if (a.related) return 1;
      return -1;
    });

  return (
    <PageContainer>
      <ProList
        dataSource={[...list]}
        loading={loading}
        grid={{ gutter: 16, xs: 1, sm: 1, md: 2, lg: 3, xl: 3, xxl: 4 }}
        split
        headerTitle={<div></div>}
        // style={{ marginTop: 24 }}
        renderItem={(item) => {
          return (
            <List.Item>
              <Link to={`/member/${item.source}/list`}>
                <Card
                  hoverable
                  // bordered={false}
                  title={item.name}
                  extra={<span>
                    {item?.relatedName ? (
                      <>
                        <InfoCircleTwoTone style={{ marginRight: '4px' }} />
                        关联{item?.relatedName}
                      </>
                    ) : (
                      ''
                    )}
                  </span>}
                // style={{ backgroundColor: '#f7f8fa' }}
                >
                  <Card.Meta
                    avatar={getAuthnHandler(item.type)?.icon}
                    title={
                      <Space>
                        <UserOutlined />
                        {item.count || 0}
                      </Space>
                    }
                    description={`由${item.authnName}认证器同步创建`}
                  // description={<Space>
                  //   <UserOutlined />{item.count || 0}
                  //   <span>
                  //     {(item?.relatedName) ? <><InfoCircleTwoTone style={{ marginRight: '4px' }} />已关联{item?.relatedName}</> : ''}</span>

                  // </Space>}
                  ></Card.Meta>
                </Card>
              </Link>
            </List.Item>
          );
        }}
      />
    </PageContainer>
  );
};

export default App;
