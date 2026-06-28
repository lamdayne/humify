package com.lamdayne.humify.task.service.impl;

import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.media.dto.response.UploadResponse;
import com.lamdayne.humify.media.service.MediaService;
import com.lamdayne.humify.task.dto.response.AttachmentResponse;
import com.lamdayne.humify.task.entity.Task;
import com.lamdayne.humify.task.entity.TaskAttachment;
import com.lamdayne.humify.task.mapper.TaskMapper;
import com.lamdayne.humify.task.repository.TaskAttachmentRepository;
import com.lamdayne.humify.task.repository.TaskRepository;
import com.lamdayne.humify.task.service.TaskAttachmentService;
import com.lamdayne.humify.user.entity.User;
import com.lamdayne.humify.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskAttachmentServiceImpl implements TaskAttachmentService {

    private final TaskRepository taskRepository;
    private final TaskAttachmentRepository taskAttachmentRepository;
    private final UserService userService;
    private final MediaService mediaService;
    private final TaskMapper taskMapper;

    @Override
    @Transactional
    public AttachmentResponse addAttachment(UserPrincipal userPrincipal, Long taskId, MultipartFile file) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));

        UploadResponse uploadResult = mediaService.upload(file, "tasks");

        User uploader = userService.getUserById(userPrincipal.getId());

        TaskAttachment attachment = TaskAttachment.builder()
                .task(task)
                .uploadedBy(uploader)
                .fileName(file.getOriginalFilename())
                .fileUrl(uploadResult.getUrl())
                .fileSize(file.getSize())
                .build();

        return taskMapper.toAttachmentResponse(taskAttachmentRepository.save(attachment));
    }

    @Override
    @Transactional
    public void deleteAttachment(Long attachmentId) {
        TaskAttachment attachment = taskAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        taskAttachmentRepository.delete(attachment);
    }
}