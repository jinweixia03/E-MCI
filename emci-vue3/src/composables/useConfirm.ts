import { ElMessageBox } from 'element-plus'

export function useConfirm() {
  const confirm = async (
    message: string,
    title: string = '确认',
    type: 'warning' | 'info' | 'success' | 'error' = 'warning'
  ): Promise<boolean> => {
    try {
      await ElMessageBox.confirm(message, title, {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type
      })
      return true
    } catch {
      return false
    }
  }

  const confirmDelete = (message: string = '确定删除吗？'): Promise<boolean> => {
    return confirm(message, '删除确认', 'warning')
  }

  return {
    confirm,
    confirmDelete
  }
}
