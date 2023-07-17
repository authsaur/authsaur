import { request } from 'umi';

export async function stateFakeItem(params: any) {
  return request(`/api/authenticator/${params.id}`, {
    method: 'PUT',
    data: {
      enabled: params?.enabled
    },
  });
}
