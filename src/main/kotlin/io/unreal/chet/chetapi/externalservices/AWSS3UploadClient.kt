package io.unreal.chet.chetapi.externalservices

import aws.sdk.kotlin.services.s3.*
import aws.sdk.kotlin.services.s3.model.BucketLocationConstraint
import kotlinx.coroutines.runBlocking
import java.util.UUID

val REGION = "us-east-2"
val BUCKET = "bucket-${UUID.randomUUID()}"
val KEY = "key"

class AWSS3UploadClient() {
    suspend fun createBucket(s3: S3Client) {
        println("Creating bucket $BUCKET...")
        s3.createBucket {
            bucket = BUCKET
            createBucketConfiguration {
                locationConstraint = BucketLocationConstraint.fromValue(REGION)
            }
        }
        println("Bucket $BUCKET created successfully!")
    }

    private suspend fun cleanUp(s3: S3Client) {
        println("Deleting object $BUCKET/$KEY...")
        s3.deleteObject {
            bucket = BUCKET
            key = KEY
        }
        println("Object $BUCKET/$KEY deleted successfully!")

        println("Deleting bucket $BUCKET...")
        s3.deleteBucket {
            bucket = BUCKET
        }
        println("Bucket $BUCKET deleted successfully!")
    }

    fun getS3Client(): S3Client {
        val s3Client = S3Client { region = REGION }
        runBlocking {
            s3Client.createBucket {
                bucket = BUCKET
                createBucketConfiguration {
                    locationConstraint = BucketLocationConstraint.UsEast2
                }
            }
        }
        return s3Client
    }
}
