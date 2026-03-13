import { ref, computed } from 'vue'

export function usePagination<T>(fetchFn: (page: number, size: number) => Promise<{ list: T[]; total: number }>) {
  const pageNum = ref(1)
  const pageSize = ref(10)
  const total = ref(0)
  const list = ref<T[]>([])
  const loading = ref(false)

  const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

  const fetchData = async () => {
    loading.value = true
    try {
      const res = await fetchFn(pageNum.value, pageSize.value)
      list.value = res.list
      total.value = res.total
    } finally {
      loading.value = false
    }
  }

  const handleSizeChange = (val: number) => {
    pageSize.value = val
    pageNum.value = 1
    fetchData()
  }

  const handleCurrentChange = (val: number) => {
    pageNum.value = val
    fetchData()
  }

  const reset = () => {
    pageNum.value = 1
    pageSize.value = 10
    list.value = []
    total.value = 0
  }

  return {
    pageNum,
    pageSize,
    total,
    totalPages,
    list,
    loading,
    fetchData,
    handleSizeChange,
    handleCurrentChange,
    reset
  }
}
