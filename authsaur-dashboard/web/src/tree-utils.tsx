export interface DataNode {
  title: string;
  key: string;
  isLeaf?: boolean;
  children?: DataNode[];
}
export const attachLeaf = (list: DataNode[]): DataNode[] => {
  const rt = list.map((node) => {
    if (node.children && node.children.length > 0) {
      return {
        ...node,
        isLeaf: false,
        children: attachLeaf(node.children),
      };
    }
    if (node.children && node.children.length == 0) {
      return {
        ...node,
        isLeaf: true,
      };
    }
    return {
      ...node,
      isLeaf: false,
    };
  })
  // console.log(rt)
  return rt;
};

export const updateTreeData = (list: DataNode[], key: React.Key, children: DataNode[]): DataNode[] => {
  const rt = list.map((node) => {
    if (node.key === key) {
      return {
        ...node,
        children,
      };
    }
    if (node.children) {
      return {
        ...node,
        children: updateTreeData(node.children, key, children),
      };
    }
    return node;
  })
  // console.log(rt)
  return attachLeaf(rt);
};
export const addTreeData = (list: DataNode[], key: React.Key, children: DataNode): DataNode[] => {

  const rt = list.map((node) => {
    if (node.key === key) {
      if (node.children) {
        const ch = node.children;
        ch.unshift(children)
        return {
          ...node,
          children: ch,
        };
      } else {
        return {
          ...node,
          children: [children],
        };

      }
    }
    if (node.children) {
      return {
        ...node,
        children: addTreeData(node.children, key, children),
      };
    }
    return node;
  })
  return attachLeaf(rt);
};
export const editTreeData = (list: DataNode[], key: React.Key, children: DataNode): DataNode[] =>
  attachLeaf(list.map((node) => {
    if (node.key === key) {
      return {
        ...node,
        title: children.title
      };
    }
    if (node.children) {
      return {
        ...node,
        children: editTreeData(node.children, key, children),
      };
    }
    return node;
  }));

export const delTreeData = (list: DataNode[], key: React.Key): DataNode[] => {
  const newTree = list.filter(x => x.key !== key)
  newTree.forEach(x => x.children && (x.children = delTreeData(x.children, key)))
  return attachLeaf(newTree);
};
