import { User } from "./User";

export type Message = {
    id: number,
    content: string,
    createdAt: number,
    modifiedAt: number,
    messageType: MessageType,
    sender: User
}

export type MessageType = 'USER_JOIN' | 'USER_LEAVE' |'USER_CHAT' | 'USER_RENAME';