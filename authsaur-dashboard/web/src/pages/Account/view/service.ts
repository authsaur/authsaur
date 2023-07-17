import { request } from 'umi';
import type { CardListItemDataType } from './data.d';

export async function queryFakeDirectoryList(params: {
  count: number;
}): Promise<{ data: { list: CardListItemDataType[] } }> {
  return request('/api/usermanagement/directory', {
    params,
  });
}

export async function queryFakeUserList(params, signal): Promise<{ data: { list: CardListItemDataType[] } }> {
  return request(`/api/usermanagement/source/${params.source}`, {
    params,
    signal,
    skipErrorHandler:true
  });
}

export async function querySubOrgList(params: {
  source: string,
  parentId: string;
}): Promise<{ data: { list: CardListItemDataType[] } }> {
  return request('/api/organization', {
    params,
  });
}

export async function fakeSubmitOrgForm(params: any) {
  return request('/api/organization', {
    method: 'POST',
    data: params,
  });
}
export async function queryFakeUserItem(params: any) {
  return request(`/api/usermanagement/${params.id}`, {
    method: 'GET',
  });
}
export async function fakeSubmitUserForm(params: any) {
  return request('/api/usermanagement', {
    method: 'POST',
    data: params,
  });
}
export async function removeFakeItem(params: any) {
  return request(`/api/organization/${params.id}`, {
    method: 'DELETE',
  });
}
export async function removeFakeUserItem(params: any) {
  return request(`/api/usermanagement/${params.id}`, {
    method: 'DELETE',
  });
}

export async function queryFakeOrgList(params: {
  count: number;
}): Promise<{ data: { list: any[] } }> {
  return request('/api/organization/search', {
    params,
  });
}
