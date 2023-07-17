import { request } from 'umi';
import type { CardListItemDataType } from './data.d';

export async function queryFakeList(params: {
  count: number;
}): Promise<{ data: { list: CardListItemDataType[] } }> {
  return request('/api/authenticator', {
    params,
  });
}

export async function queryAuthnList(params: {
  count: number;
}): Promise<{ data: { list: CardListItemDataType[] } }> {
  return request('/api/authenticator', {
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
