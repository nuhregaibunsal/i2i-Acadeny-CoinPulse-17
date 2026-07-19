interface AvatarProps {
  avatar: string | null
  name: string
  size?: number
}

export function Avatar({ avatar, name, size = 38 }: AvatarProps) {
  const style = { width: size, height: size }
  if (avatar) {
    return <img className="avatar-img" src={avatar} alt="" style={style} />
  }
  return (
    <span className="avatar" style={style}>
      {(name || '?').charAt(0).toUpperCase()}
    </span>
  )
}
