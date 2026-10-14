import { useEffect, useRef, useState } from 'react'
import { getProfileApi, updateProfileApi } from '../../api/apiService'
import Loading from '../../components/common/Loading'

const PROFILE_LINKS = [
  { label: 'GitHub', description: 'Mã nguồn dự án', field: 'githubUrl' },
  { label: 'Figma', description: 'Thiết kế giao diện', field: 'figmaUrl' },
  { label: 'Postman', description: 'Tài liệu và kiểm thử API', field: 'postmanUrl' },
  { label: 'PDF báo cáo', description: 'Báo cáo đồ án', field: 'reportUrl' },
]

function InfoField({ label, value }) {
  return (
    <div className="profile-info-field">
      <dt>{label}</dt>
      <dd>{value}</dd>
    </div>
  )
}

function ProjectLink({ label, description, href }) {
  if (!href) {
    return (
      <div className="profile-project-link">
        <strong>{label}</strong>
        <span>Chưa có liên kết</span>
      </div>
    )
  }

  return (
    <a className="profile-project-link" href={href} target="_blank" rel="noreferrer">
      <strong>
        {label} <span aria-hidden="true">↗</span>
      </strong>
      <span>{description}</span>
    </a>
  )
}

export default function ProfilePage() {
  const [profile, setProfile] = useState(null)
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState('')
  const [uploadMessage, setUploadMessage] = useState(null)
  const [uploading, setUploading] = useState(false)
  const [reloadKey, setReloadKey] = useState(0)
  const fileInputRef = useRef(null)

  useEffect(() => {
    let active = true
    setLoading(true)
    setLoadError('')

    getProfileApi()
      .then((data) => {
        if (active) setProfile(data)
      })
      .catch(() => {
        if (active) setLoadError('Không thể tải thông tin cá nhân từ máy chủ.')
      })
      .finally(() => {
        if (active) setLoading(false)
      })

    return () => {
      active = false
    }
  }, [reloadKey])

  const handleAvatarChange = (event) => {
    const input = event.target
    const file = input.files?.[0]
    if (!file || !profile) return

    setUploadMessage(null)
    if (!file.type.startsWith('image/')) {
      setUploadMessage({ text: 'Vui lòng chọn một file ảnh.', type: 'error' })
      input.value = ''
      return
    }

    setUploading(true)
    const reader = new FileReader()
    reader.onload = async () => {
      try {
        const updated = await updateProfileApi({ ...profile, avatarUrl: String(reader.result || '') })
        setProfile(updated)
        setUploadMessage({ text: 'Đã lưu ảnh đại diện.', type: 'success' })
      } catch {
        setUploadMessage({ text: 'Không thể lưu ảnh đại diện. Vui lòng thử lại.', type: 'error' })
      } finally {
        setUploading(false)
        input.value = ''
      }
    }
    reader.onerror = () => {
      setUploadMessage({ text: 'Không thể đọc file ảnh. Vui lòng thử lại.', type: 'error' })
      setUploading(false)
      input.value = ''
    }
    reader.readAsDataURL(file)
  }

  if (loading) return <Loading text="Đang tải thông tin cá nhân..." />

  if (loadError || !profile) {
    return (
      <div className="profile-page" role="alert">
        <p>{loadError || 'Không tìm thấy thông tin cá nhân.'}</p>
        <button type="button" className="primary-btn" onClick={() => setReloadKey((current) => current + 1)}>
          Thử lại
        </button>
      </div>
    )
  }

  const initials = String(profile.fullName || '')
    .trim()
    .split(/\s+/)
    .filter(Boolean)
    .slice(-2)
    .map((part) => part[0])
    .join('')
    .toUpperCase() || '?'

  return (
    <div className="profile-page">
      <div className="profile-layout">
        <section className="profile-preview-card" aria-label="Ảnh đại diện sinh viên">
          <div className="profile-avatar">
            {profile.avatarUrl ? <img src={profile.avatarUrl} alt="Ảnh đại diện" /> : <span>{initials}</span>}
          </div>

          <input
            ref={fileInputRef}
            className="avatar-file-input"
            type="file"
            accept="image/*"
            aria-label="Chọn ảnh đại diện"
            onChange={handleAvatarChange}
            disabled={uploading}
          />
          <button
            className="avatar-upload-btn"
            type="button"
            onClick={() => fileInputRef.current?.click()}
            disabled={uploading}
          >
            {uploading ? 'Đang lưu...' : 'Tải ảnh lên'}
          </button>
          {uploadMessage && (
            <p className={`profile-avatar-message ${uploadMessage.type}`} role={uploadMessage.type === 'error' ? 'alert' : 'status'}>
              {uploadMessage.text}
            </p>
          )}

          <h2>{profile.fullName || '--'}</h2>
          <p>{profile.studentCode || '--'}</p>
          <span className="class-badge">{profile.className || '--'}</span>
        </section>

        <section className="profile-details-card" aria-labelledby="profile-info-title">
          <div className="profile-section-heading">
            <h2 id="profile-info-title">Thông tin cá nhân</h2>
          </div>

          <dl className="profile-info-grid">
            <InfoField label="Họ và tên" value={profile.fullName || '--'} />
            <InfoField label="Mã sinh viên" value={profile.studentCode || '--'} />
            <InfoField label="Lớp" value={profile.className || '--'} />
            <InfoField label="Email" value={profile.email || '--'} />
          </dl>

          <div className="profile-divider" />

          <div className="profile-section-heading">
            <h2>Liên kết dự án</h2>
            <p>Mã nguồn, thiết kế và tài liệu của dự án.</p>
          </div>

          <div className="profile-links-grid">
            {PROFILE_LINKS.map(({ field, ...link }) => (
              <ProjectLink key={field} {...link} href={profile[field]} />
            ))}
          </div>
        </section>
      </div>
    </div>
  )
}
