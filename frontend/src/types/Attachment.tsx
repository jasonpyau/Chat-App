export type Attachment = {
    id: number,
    createdAt: number,
    attachmentType: AttachmentType,
    url: string,
    fileName: string,
    fileHash: string,
    fileByteSize: number,
    fileCompressByteSize: number,
}

export type AttachmentType = 'image/png' | 'image/jpeg' |'image/gif';