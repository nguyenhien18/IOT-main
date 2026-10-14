import { useEffect, useState } from 'react'
import DataTable, { EmptyState, TableFooter } from '../../components/common/DataTable'
import Icon from '../../components/common/Icon'
import { formatDate } from '../../utils/dateTime'
import { getSensorLabel } from '../../utils/sensorLabels'
import Loading from '../../components/common/Loading'
import { getSensorHistoryApi, getSensorsApi } from '../../api/apiService'

const DEFAULT_PAGE_SIZE = 10

function createSensorHistoryParams(search, page, size, direction) {
  const params = { page, size, sort: `createdAt,${direction}` }
  if (!search) return params

  const value = search.value.trim()
  if (search.field === 'time') {
    if (value) params.time = value
    return params
  }

  if (search.field === 'all') {
    if (value) params.keyword = value
    return params
  }

  params.sensorId = search.field.replace('sensor-', '')
  if (value) params.value = value.replace(',', '.')
  return params
}

function getSensorName(row) {
  return row.sensorName || row.sensor?.name || ''
}

function getSensorTime(row) {
  return row.createdAt || row.timestamp || row.time
}

function formatSensorValue(row) {
  const value = row.value ?? '--'
  const sensorName = String(getSensorName(row)).toLowerCase()

  if (value === '--') return value
  if (sensorName === 'temperature') return `${value} °C`
  if (sensorName === 'humidity') return `${value} %`
  if (sensorName === 'light') return `${value} Lux`
  return value
}

export default function SensorHistoryPage() {
  const [sensors, setSensors] = useState([])
  const [sensorRows, setSensorRows] = useState([])
  const [searchField, setSearchField] = useState('all')
  const [searchInput, setSearchInput] = useState('')
  const [appliedSearch, setAppliedSearch] = useState(null)
  const [sortDirection, setSortDirection] = useState('desc')
  const [page, setPage] = useState(0)
  const [pageSize, setPageSize] = useState(DEFAULT_PAGE_SIZE)
  const [totalPages, setTotalPages] = useState(1)
  const [totalElements, setTotalElements] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const loadData = async (
    nextPage = 0,
    activeSearch = appliedSearch,
    activePageSize = pageSize,
    direction = sortDirection,
  ) => {
    setLoading(true)
    setError('')

    try {
      const data = await getSensorHistoryApi(
        createSensorHistoryParams(activeSearch, nextPage, activePageSize, direction),
      )
      const nextRows = data.content || data

      setSensorRows(nextRows)
      setPage(nextPage)
      setPageSize(activePageSize)
      setAppliedSearch(activeSearch)
      setSortDirection(direction)
      setTotalPages(Math.max(1, data.totalPages || 1))
      setTotalElements(data.totalElements ?? nextRows.length)
    } catch {
      setError('Không thể tải dữ liệu cảm biến. Vui lòng tải lại trang.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    getSensorsApi()
      .then((data) => setSensors(data.content || data))
      .catch(() => setError('Không thể tải danh sách cảm biến. Vui lòng tải lại trang.'))
    loadData(0, null, DEFAULT_PAGE_SIZE, 'desc')
  }, [])

  const handleFieldChange = (event) => {
    setSearchField(event.target.value)
    setSearchInput('')
    loadData(0, null)
  }

  const handleSearch = (event) => {
    event.preventDefault()
    const value = searchInput.trim()
    loadData(0, { field: searchField, value })
  }

  const handleSortChange = (event) => {
    loadData(0, appliedSearch, pageSize, event.target.value)
  }

  const handlePageSizeChange = (nextPageSize) => {
    loadData(0, appliedSearch, nextPageSize)
  }

  const isAll = searchField === 'all'
  const isTime = searchField === 'time'
  let searchPlaceholder = 'Nhập giá trị cần tìm...'
  if (isAll) searchPlaceholder = 'Nhập nội dung cần tìm...'
  if (isTime) searchPlaceholder = 'VD: 10:45 hoặc 18/08/2026 10:45'

  return (
    <div className="content-page">
      <form className="filter-box sensor-filter" onSubmit={handleSearch}>
        <div className="filter-field">
          <select
            aria-label="Chọn trường tìm kiếm"
            value={searchField}
            onChange={handleFieldChange}
            disabled={loading}
          >
            <option value="all">Tất cả</option>
            {sensors.map((sensor) => (
              <option key={sensor.id} value={`sensor-${sensor.id}`}>
                {getSensorLabel(sensor.name)}
              </option>
            ))}
            <option value="time">Thời gian</option>
          </select>
        </div>

        <div className="sensor-search-control">
          <div className="filter-field search-field">
            <Icon name="search" size={18} />
            <input
              type={isAll || isTime ? 'text' : 'number'}
              step={isAll || isTime ? undefined : 'any'}
              aria-label={
                isTime
                  ? 'Nhập thời gian cần tìm'
                  : isAll
                    ? 'Nhập nội dung cần tìm'
                    : 'Nhập giá trị cảm biến'
              }
              aria-describedby={isTime ? 'sensor-time-hint' : undefined}
              value={searchInput}
              onChange={(event) => setSearchInput(event.target.value)}
              placeholder={searchPlaceholder}
              disabled={loading}
              autoComplete="off"
            />
          </div>
          {isTime && (
            <small className="sensor-time-hint" id="sensor-time-hint">
              Định dạng: HH:mm, HH:mm:ss hoặc DD/MM/YYYY HH:mm.
            </small>
          )}
        </div>

        <div className="filter-field">
          <select
            aria-label="Sắp xếp thời gian"
            value={sortDirection}
            onChange={handleSortChange}
            disabled={loading}
          >
            <option value="desc">Thời gian: Mới nhất </option>
            <option value="asc">Thời gian: Cũ nhất </option>
          </select>
        </div>

        <div className="filter-buttons">
          <button className="primary-btn" type="submit" disabled={loading}>
            <Icon name="search" size={16} />
            {loading ? 'Đang tải...' : 'Tìm'}
          </button>
        </div>
      </form>

      {error && (
        <p className="sensor-load-error" role="alert">
          {error}
        </p>
      )}

      {loading ? (
        <Loading />
      ) : (
        <DataTable
          columns={[
            'STT',
            'Loại cảm biến',
            'Giá trị',
            'Thời gian',
          ]}
          label="Dữ liệu cảm biến"
        >
          {sensorRows.length === 0 ? (
            <EmptyState colSpan={4} />
          ) : (
            sensorRows.map((row, index) => (
              <tr key={row.id}>
                <td>{page * pageSize + index + 1}</td>
                <td>{getSensorLabel(getSensorName(row))}</td>
                <td>{formatSensorValue(row)}</td>
                <td>{formatDate(getSensorTime(row))}</td>
              </tr>
            ))
          )}
        </DataTable>
      )}

      <TableFooter
        page={page}
        pageSize={pageSize}
        pageSizeOptions={[5, 10, 20]}
        totalPages={totalPages}
        totalElements={totalElements}
        rowCount={sensorRows.length}
        loading={loading}
        onChange={(nextPage) => loadData(nextPage)}
        onPageSizeChange={handlePageSizeChange}
      />
    </div>
  )
}
