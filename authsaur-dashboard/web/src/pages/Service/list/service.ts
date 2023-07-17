import { request } from 'umi';
import type { CardListItemDataType } from './data.d';

export async function queryFakeList(params: any): Promise<{ data: { list: CardListItemDataType[] } }> {
  return request('/api/service', {
    params,
  });
  
}
export async function queryServiceList(params: any): Promise<{ data: { list: CardListItemDataType[] } }> {
  return request('/api/service', {
    params,
  }).then((res) => {
    return {
      data: res.data?.list || [],
      // total: res.data.total,
      // success: res.success,
      // pageSize: res.pageSize,
      // current: res.current
    };
  });
  
}
