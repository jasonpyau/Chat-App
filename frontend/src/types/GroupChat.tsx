import { User } from "./User";

export type GroupChat = {
    id: number,
    name: string,
    users: User[],
    lastMessageAt: number
}