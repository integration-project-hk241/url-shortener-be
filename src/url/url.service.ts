import {
  BadRequestException,
  Injectable,
  NotFoundException,
  UnprocessableEntityException,
} from '@nestjs/common';
import { CreateUrlDto } from './dto/create-url.dto';
import { InjectRepository } from '@nestjs/typeorm';
import { Url } from './entities/url.entity';
import { Repository } from 'typeorm';
import { isURL } from 'class-validator';
import { nanoid } from 'nanoid';

@Injectable()
export class UrlService {
  constructor(@InjectRepository(Url) private urlRepository: Repository<Url>) {}

  async createShortUrl(url: CreateUrlDto) {
    const { longUrl } = url;

    if (!isURL(longUrl)) {
      throw new BadRequestException('String Must be a Valid Url');
    }

    const urlCode = nanoid(10);
    const baseUrl = 'http://localhost:3000';

    try {
      let url = await this.urlRepository.findOneBy({ longUrl });

      if (url) {
        return url.shortUrl;
      }

      const shortUrl = `${baseUrl}/${urlCode}`;

      url = this.urlRepository.create({
        urlCode,
        longUrl,
        shortUrl,
      });

      this.urlRepository.save(url);

      return url.shortUrl;
    } catch (error) {
      console.error(error);
      throw new UnprocessableEntityException('Server Error');
    }
  }

  async redirectUrl(urlCode: string): Promise<string> {
    try {
      const url = await this.urlRepository.findOneBy({ urlCode });

      if (!url) {
        throw new NotFoundException('Resource Not Found');
      }

      let fullUrl = url.longUrl;

      if (!/^https?:\/\//i.test(fullUrl)) {
        // If not, prepend 'http://'
        fullUrl = `http://${fullUrl}`;
      }

      return fullUrl;
    } catch (error) {
      console.error(error);
      throw new NotFoundException('Resource Not Found');
    }
  }
}
