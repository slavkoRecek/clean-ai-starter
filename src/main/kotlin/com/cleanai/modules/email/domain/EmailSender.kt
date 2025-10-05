package com.cleanai.modules.email.domain

interface EmailSender {
    fun sendTemplatedEmail(email: TemplatedEmail): EmailResult
}
