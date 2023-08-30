export type User = {
    id: number,
    username: string,
    displayName: string,
    avatarURL?: string,
    authenticationProvider: AuthenticationProvider,
    role: Role,
    createdAt: number
}

export const DefaultPicture: string = 'https://lh3.googleusercontent.com/-cXXaVVq8nMM/AAAAAAAAAAI/AAAAAAAAAKI/_Y1WfBiSnRI/photo.jpg';

type Role = 'NEW_USER' | 'USER' |'ADMIN';

type AuthenticationProvider = 'GITHUB' | 'GOOGLE' | 'DISCORD';