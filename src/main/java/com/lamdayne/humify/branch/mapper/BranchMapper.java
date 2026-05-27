package com.lamdayne.humify.branch.mapper;

import com.lamdayne.humify.branch.dto.request.CreateBranchRequest;
import com.lamdayne.humify.branch.dto.response.BranchResponse;
import com.lamdayne.humify.branch.entity.Branch;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;

@Mapper(componentModel = "spring")
public interface BranchMapper {

    Branch toBranch(CreateBranchRequest request);

    @Mapping(source = "company.id", target = "companyId")
    BranchResponse toBranchResponse(Branch branch);

    @Mapping(source = "company.id", target = "companyId")
    List<BranchResponse> toBranchResponseList(List<Branch> branches);

}